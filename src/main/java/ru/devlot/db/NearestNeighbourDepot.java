package ru.devlot.db;

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

import static ru.devlot.LotDeveloperEngine.SLEEP_TIME;
import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.db.SpreadsheetDepot.InfoDepot;
import static ru.devlot.model.Factor.Feature;

@ThreadSafe
public class NearestNeighbourDepot {

    private DataDepot dataDepot;
    private InfoDepot infoDepot;

    private Map<String, Attribute> attributes;

    private Spreadsheet data;
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
        Spreadsheet data = dataDepot.get();
        Spreadsheet info = infoDepot.get();

        Attribute idAttribute = new Attribute(ID, (List<String>) null);

        Map<String, Attribute> attributes = new HashMap<>();
        for (Feature feature : data.getFeatures()) {
            attributes.put(feature.getName(), new Attribute(feature.getName()));
        }
        attributes.put(ID, idAttribute);

        Instances learn = new Instances("knn", new ArrayList<>(attributes.values()), data.size());
        learn.setClass(idAttribute);
        for (Vector x : data) {
            if (!info.get(x.getId()).contains(REFERENCE)) {
                continue;
            }

            Instance instance = new DenseInstance(attributes.size());
            for (Feature feature : data.getFeatures()) {
                instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
            }
            instance.setValue(idAttribute, x.getId());

            learn.add(instance);
        }

        LinearNNSearch search = new LinearNNSearch(learn);

        synchronized (this) {
            this.data = data;
            this.info = info;
            this.attributes = attributes;
            this.search = search;
        }
    }

    public synchronized List<Info> getKNearestNeighbours(Map<String, Double> x, int k) throws Exception {
        Instance instance = new DenseInstance(attributes.size());
        for (Feature feature : data.getFeatures()) {
            instance.setValue(attributes.get(feature.getName()), x.get(feature.getName()));
        }

        List<Info> neighbours = new ArrayList<>();
        for (Instance neighbour : search.kNearestNeighbours(instance, k)) {
            String id = neighbour.stringValue(attributes.get(ID));
            neighbours.add(new Info(id, info.get(id).get(REFERENCE)));
        }

        return neighbours;
    }

    @Required
    public void setDataDepot(DataDepot dataDepot) {
        this.dataDepot = dataDepot;
    }

    @Required
    public void setInfoDepot(InfoDepot infoDepot) {
        this.infoDepot = infoDepot;
    }

}
