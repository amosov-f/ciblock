package ru.devlot.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Required;
import ru.devlot.model.Info;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.devlot.CiBlockServer.SLEEP_TIME;
import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.db.SpreadsheetDepot.InfoDepot;
import static ru.devlot.model.Factor.Feature;

@ThreadSafe
public class NearestNeighbourDepot {
    @NotNull
    private DataDepot dataDepot;
    @NotNull
    private InfoDepot infoDepot;

    @NotNull
    private Map<String, Attribute> attributes;

    @NotNull
    private Spreadsheet data;
    @NotNull
    private Spreadsheet info;

    private LinearNNSearch search;

    private static final String REFERENCE = "ссылка";
    private static final String ID = "id";

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    train();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void train() throws Exception {
        final Spreadsheet data = dataDepot.get();
        final Spreadsheet info = infoDepot.get();

        final Attribute idAttribute = new Attribute(ID, (List<String>) null);

        final Map<String, Attribute> attributes = new HashMap<>();
        for (final Feature feature : data.getFeatures()) {
            attributes.put(feature.getName(), new Attribute(feature.getName()));
        }
        attributes.put(ID, idAttribute);

        final Instances learn = new Instances("knn", new ArrayList<>(attributes.values()), data.size());
        learn.setClass(idAttribute);
        for (final Vector x : data) {
            if (!info.get(x.getId()).contains(REFERENCE)) {
                continue;
            }

            final Instance instance = new DenseInstance(attributes.size());
            for (final Feature feature : data.getFeatures()) {
                instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
            }
            instance.setValue(idAttribute, x.getId());

            learn.add(instance);
        }

        final LinearNNSearch search = new LinearNNSearch(learn);

        synchronized (this) {
            this.data = data;
            this.info = info;
            this.attributes = attributes;
            this.search = search;
        }
    }

    @NotNull
    public synchronized List<Info> getKNearestNeighbours(@NotNull final Map<String, Double> x, final int k) throws Exception {
        final Instance instance = new DenseInstance(attributes.size());
        for (final Feature feature : data.getFeatures()) {
            instance.setValue(attributes.get(feature.getName()), x.get(feature.getName()));
        }

        final List<Info> neighbours = new ArrayList<>();
        for (final Instance neighbour : search.kNearestNeighbours(instance, k)) {
            final String id = neighbour.stringValue(attributes.get(ID));
            neighbours.add(new Info(id, info.get(id).get(REFERENCE)));
        }

        return neighbours;
    }

    @Required
    public void setDataDepot(@NotNull final DataDepot dataDepot) {
        this.dataDepot = dataDepot;
    }

    @Required
    public void setInfoDepot(@NotNull final InfoDepot infoDepot) {
        this.infoDepot = infoDepot;
    }

}
