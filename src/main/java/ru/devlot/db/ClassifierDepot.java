package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.model.Factor;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ClassifierDepot {

    private SpreadsheetDepot spreadsheetDepot;

    private Map<Integer, Classifier> classifiers;

    private Spreadsheet spreadsheet;

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                spreadsheet = spreadsheetDepot.getSpreadsheet();
                try {
                    classifiers = train();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(classifiers);
                System.out.println();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Map<Integer, Double> classify(Map<Integer, Double> features) throws Exception {

        Instance instance = new DenseInstance(features.size());

        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i : features.keySet()) {
            attributes.add(new Attribute(spreadsheet.getFactor(i).name));
            instance.setValue(i, features.get(i));
        }

        Map<Integer, Double> answers = new HashMap<>();
        for (int i : classifiers.keySet()) {
            Attribute attribute = new Attribute(spreadsheet.getFactor(i).name);

            attributes.add(attribute);

            Instances test = new Instances("test", attributes, 1);
            test.add(instance);

            answers.put(i, classifiers.get(i).classifyInstance(instance));

            attributes.remove(attribute);
        }

        return answers;
    }

    private Map<Integer, Classifier> train() throws Exception {
        Map<Integer, Classifier> classifiers = new HashMap<>();
        for (int i : spreadsheet.getAnswers().keySet()) {
            classifiers.put(i, train(i));
        }
        return classifiers;
    }

    private Classifier train(int answerIndex) throws Exception {
        Map<Integer, Factor> features = spreadsheet.getFeatures();
        Factor answer = spreadsheet.getAnswers().get(answerIndex);


        Map<Integer, Attribute> attributes = new TreeMap<>();
        for (int i : features.keySet()) {
            attributes.put(i, new Attribute(features.get(i).name));
        }
        attributes.put(answerIndex, new Attribute(answer.name));

        Instances learn = new Instances(answer.name, new ArrayList<>(attributes.values()), spreadsheet.size());
        for (Vector x : spreadsheet) {
            Instance instance = new DenseInstance(features.size() + 1);
            for (int i : attributes.keySet()) {
                instance.setValue(attributes.get(i), x.get(i));
            }
            learn.add(instance);
        }
        learn.setClassIndex(features.size());
        System.out.println(learn);

        Classifier classifier = new LinearRegression();
        classifier.buildClassifier(learn);

        Evaluation evaluation = new Evaluation(learn);
        evaluation.evaluateModel(classifier, learn);
        System.out.println(evaluation.toSummaryString());

        return classifier;
    }

    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    @Required
    public void setSpreadsheetDepot(SpreadsheetDepot spreadsheetDepot) {
        this.spreadsheetDepot = spreadsheetDepot;
    }

}
