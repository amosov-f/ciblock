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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.db.SpreadsheetDepot.InfoDepot;
import static ru.devlot.model.Factor.Feature;

public class NearestNeighbourDepot {

    private DataDepot dataDepot;
    private InfoDepot infoDepot;

    private Map<String, Attribute> attributes;

    private Spreadsheet data;
    private Spreadsheet info;

    private LinearNNSearch search;

    private Instances learnInstances;
    private List<String> learnIds;

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                data = dataDepot.get();
                info = infoDepot.get();

                try {
                    initAttributes();
                    train();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private synchronized void train() throws Exception {
        learnIds = new ArrayList<>();
        learnInstances = new Instances("knn", new ArrayList<>(attributes.values()), data.size());
        for (Vector x : data) {
            if (info.get(x.getId()).contains("ссылка")) {
                learnIds.add(x.getId());
                learnInstances.add(toInstance(x));
            }
        }
        search = new LinearNNSearch(learnInstances);
    }

    public List<Info> getKNearestNeighbours(Map<String, Double> x, int k) throws Exception {
        Instance instance = new DenseInstance(attributes.size());
        for (Feature feature : data.getFactors(Feature.class)) {
            instance.setValue(attributes.get(feature.getName()), x.get(feature.getName()));
        }

        List<Info> neighbours = new ArrayList<>();
        for (Instance neighbour : search.kNearestNeighbours(instance, k)) {
            neighbours.add(toInfo(neighbour));
        }

        return neighbours;
    }

    private Instance toInstance(Vector x) {
        Instance instance = new DenseInstance(attributes.size());
        for (Feature feature : data.getFactors(Feature.class)) {
            instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
        }
        return instance;
    }

    private synchronized void initAttributes() {
        attributes = new HashMap<>();
        for (Feature feature : data.getFactors(Feature.class)) {
            attributes.put(feature.getName(), new Attribute(feature.getName()));
        }
    }

    public Info toInfo(Instance instance) {
        for (int i = 0; i < learnInstances.size(); ++i) {
            if (learnInstances.get(i).toString().equals(instance.toString())) {
                return new Info(learnIds.get(i), info.get(learnIds.get(i)).get("ссылка"));
            }
        }
        return null;
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
