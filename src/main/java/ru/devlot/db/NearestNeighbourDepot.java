package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.db.spreadsheet.DataDepot;
import ru.devlot.db.spreadsheet.InfoDepot;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

import java.util.*;

public class NearestNeighbourDepot {

    private DataDepot dataDepot;
    private InfoDepot infoDepot;


    private Map<Integer, Attribute> attributes;

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
            learnIds.add(x.getId());
            learnInstances.add(toInstance(x));
        }
        search = new LinearNNSearch(learnInstances);
    }

    public List<String> getKNearestNeighbours(Map<Integer, Double> x, int k) throws Exception {
        Instance instance = new DenseInstance(attributes.size());
        for (int i : data.getFeatures().keySet()) {
            instance.setValue(attributes.get(i), x.get(i));
        }

        List<String> neighbours = new ArrayList<>();
        for (Instance neighbour : search.kNearestNeighbours(instance, k)) {
            neighbours.add(getURL(neighbour));
        }

        return neighbours;
    }

    private Instance toInstance(Vector x) {
        Instance instance = new DenseInstance(attributes.size());
        for (int i : data.getFeatures().keySet()) {
            instance.setValue(attributes.get(i), x.getDouble(i));
        }
        return instance;
    }

    private synchronized void initAttributes() {
        attributes = new HashMap<>();
        for (int i : data.getFeatures().keySet()) {
            attributes.put(i, new Attribute(data.getFeatures().get(i).getName()));
        }
    }

    public String getURL(Instance instance) {
        for (int i = 0; i < learnInstances.size(); ++i) {
            if (learnInstances.get(i).toString().equals(instance.toString())) {
                return info.get(learnIds.get(i)).get(info.getFactorIndex("ссылка"));
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
