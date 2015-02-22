package ru.spbu.astro.ciblock.depot;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.CityBlockInfo;
import ru.spbu.astro.ciblock.commons.Spreadsheet;
import ru.spbu.astro.ciblock.commons.*;
import ru.spbu.astro.ciblock.commons.Vector;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@ThreadSafe
public final class NearestNeighbourDepot {
    private static final Logger LOG = Logger.getLogger(NearestNeighbourDepot.class.getName());

    private static final String REFERENCE = "ссылка";
    private static final String ID = "id";
    
    private LinearNNSearch search;
    private Map<String, Attribute> attributes;
    
    private volatile Spreadsheet data;
    private volatile Spreadsheet info;
    
    @NotNull
    private final SpreadsheetDepot.DataDepot dataDepot;
    @NotNull
    private final SpreadsheetDepot.InfoDepot infoDepot;
    
    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    public NearestNeighbourDepot(@NotNull final SpreadsheetDepot.DataDepot dataDepot,
                                 @NotNull final SpreadsheetDepot.InfoDepot infoDepot,
                                 @NotNull final Properties properties) 
    {
        this.dataDepot = dataDepot;
        this.infoDepot = infoDepot;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (train()) {
                    LOG.info("Nearest neighbour search training completed");
                } else {
                    LOG.info("Spreadsheets hasn't changed");
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Training error!", e);
            }
        }, 0, Long.parseLong(properties.getProperty("ciblock.nearest_neighbour.retry_delay")), TimeUnit.SECONDS);
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    private boolean train() throws Exception {
        final Spreadsheet data = dataDepot.get();
        final Spreadsheet info = infoDepot.get();
        if (data.equals(this.data) && info.equals(this.info)) {
            return false;
        }

        final Attribute idAttribute = new Attribute(ID, (List<String>) null);

        final Map<String, Attribute> attributes = new HashMap<>();
        for (final Factor.Feature feature : data.getFeatures()) {
            attributes.put(feature.getName(), new Attribute(feature.getName()));
        }
        attributes.put(ID, idAttribute);

        LOG.info("Prepearing dataset...");
        final Instances learn = new Instances("knn", new ArrayList<>(attributes.values()), data.size());
        learn.setClass(idAttribute);
        for (final Vector x : data.getVectors()) {
            if (!info.get(x.getId()).contains(REFERENCE)) {
                continue;
            }

            final Instance instance = new DenseInstance(attributes.size());
            for (final Factor.Feature feature : data.getFeatures()) {
                instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
            }
            instance.setValue(idAttribute, x.getId());

            learn.add(instance);
        }

        LOG.info("Training...");
        final LinearNNSearch search = new LinearNNSearch(learn);

        synchronized (this) {
            this.search = search;
            this.attributes = attributes;
            this.data = data;
            this.info = info;
        }
        latch.countDown();
        return true;
    }

    @NotNull
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public CityBlockInfo[] getKNearestNeighbours(@NotNull final Map<String, Double> x, final int k) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        final LinearNNSearch search;
        final Map<String, Attribute> attributes;
        final Spreadsheet data;
        final Spreadsheet info;
        synchronized (this) {
            search = this.search;
            attributes = this.attributes;
            data = this.data;
            info = this.info;
        }

        final Instance instance = new DenseInstance(attributes.size());
        for (final Factor.Feature feature : data.getFeatures()) {
            instance.setValue(attributes.get(feature.getName()), x.get(feature.getName()));
        }

        final List<CityBlockInfo> neighbours = new ArrayList<>();
        try {
            for (final Instance neighbour : search.kNearestNeighbours(instance, k)) {
                final String id = neighbour.stringValue(attributes.get(ID));
                final String ref = info.get(id).get(REFERENCE);
                if (ref != null) {
                    neighbours.add(new CityBlockInfo(id, ref));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return neighbours.toArray(new CityBlockInfo[neighbours.size()]);
    }
}
