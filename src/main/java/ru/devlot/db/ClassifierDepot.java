package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import ru.devlot.model.factor.Answer;
import ru.devlot.model.factor.Class;
import ru.devlot.model.factor.Feature;
import ru.devlot.model.factor.Regression;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;


public class ClassifierDepot {

    private SpreadsheetDepot spreadsheetDepot;

    private Map<Integer, Classifier> classifiers;

    private Spreadsheet spreadsheet;

    private static final Map<java.lang.Class<? extends Answer>, java.lang.Class<? extends Classifier>> type2classifier = new HashMap<>();
    static {
        type2classifier.put(Regression.class, LinearRegression.class);
        type2classifier.put(Class.class, SMO.class);
    }

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
            attributes.add(new Attribute(spreadsheet.getFactor(i).getName()));
            instance.setValue(i, features.get(i));
        }

        Map<Integer, Double> answers = new HashMap<>();
        for (int i : classifiers.keySet()) {
            Attribute attribute = new Attribute(spreadsheet.getFactor(i).getName());

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
            Classifier classifier = train(i);
            classifiers.put(i, classifier);
            System.out.println(i + ": " + classifier);
            System.out.println(classifiers);
        }

        return classifiers;
    }


    private Classifier train(int answerIndex) throws Exception {
        Map<Integer, Feature> features = spreadsheet.getFeatures();
        Answer answer = spreadsheet.getAnswers().get(answerIndex);

        Map<Integer, Attribute> attributes = new TreeMap<>();
        for (int i : features.keySet()) {
            attributes.put(i, new Attribute(features.get(i).getName()));
        }
        if (answer instanceof Class) {
            attributes.put(answerIndex, new Attribute(answer.getName(), ((Class) answer).getClasses()));
        } else {
            attributes.put(answerIndex, new Attribute(answer.getName()));
        }

        Instances learn = new Instances(answer.getName(), new ArrayList<>(attributes.values()), spreadsheet.size());
        a:
        for (Vector x : spreadsheet) {
            Instance instance = new DenseInstance(features.size() + 1);
            for (int i : attributes.keySet()) {
                if (!x.contains(i)) {
                    continue a;
                }
                if (spreadsheet.getFactor(i) instanceof Class) {
                    instance.setValue(attributes.get(i), x.get(i));
                } else {
                    instance.setValue(attributes.get(i), x.getDouble(i));
                }
            }
            learn.add(instance);
        }
        learn.setClassIndex(features.size());
        System.out.println(learn);

        Classifier classifier = type2classifier.get(answer.getClass()).getConstructor().newInstance();
        classifier.buildClassifier(learn);

        Evaluation evaluation = new Evaluation(learn);
        evaluation.crossValidateModel(classifier, learn, 2, new Random());
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
