package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.db.spreadsheet.DataDepot;
import ru.devlot.model.Factor;
import ru.devlot.model.Factor.Answer;
import ru.devlot.model.Factor.Class;
import ru.devlot.model.Factor.Regression;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ru.devlot.model.Factor.Feature;


public class ClassifierDepot {

    private DataDepot dataDepot;

    private Map<String, Classifier> classifiers;

    private Spreadsheet data;
    private Map<String, Attribute> attributes;

    private static final Map<
            java.lang.Class<? extends Answer>,
            java.lang.Class<? extends Classifier>
    > type2classifier = new HashMap<>();
    static {
        type2classifier.put(Regression.class, LinearRegression.class);
        type2classifier.put(Class.class, SMO.class);
    }

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                data = dataDepot.get();

                try {
                    initAttributes();
                    classifiers = train();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (classifiers != null) {
                    System.out.println(classifiers.values());
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Map<String, Double> classify(Map<String, Double> features) throws Exception {
        Instance instance = new DenseInstance(attributes.size());
        for (String name : features.keySet()) {
            instance.setValue(attributes.get(name), features.get(name));
        }

        Map<String, Double> answers = new HashMap<>();
        for (String name : classifiers.keySet()) {
            answers.put(name, classifiers.get(name).classifyInstance(instance));
        }

        return answers;
    }

    private Map<String, Classifier> train() throws Exception {
        System.out.println("Train!");

        Map<String, Classifier> classifiers = new HashMap<>();
        for (Answer answer : data.getFactors(Answer.class)) {
            Classifier classifier = train(answer);
            classifiers.put(answer.getName(), classifier);
        }

        return classifiers;
    }


    private Classifier train(Answer answer) throws Exception {
        System.out.println("Train " + answer + "!");

        Instances learn = new Instances(answer.getName(), new ArrayList<>(attributes.values()), data.size());

        for (Vector x : data) {
            Instance instance = toInstance(x, answer.getName());
            if (instance != null) {
                learn.add(instance);
            }
        }
        learn.setClass(attributes.get(answer.getName()));

        Classifier classifier = type2classifier.get(answer.getClass()).getConstructor().newInstance();
        classifier.buildClassifier(learn);

        Evaluation evaluation = new Evaluation(learn);
        evaluation.crossValidateModel(classifier, learn, 3, new Random());
        System.out.println(evaluation.toSummaryString());

        return classifier;
    }

    private synchronized void initAttributes() {
        attributes = new HashMap<>();
        for (Factor factor : data.getFactors()) {
            attributes.put(factor.getName(), new Attribute(factor.getName()));
        }
    }

    private Instance toInstance(Vector x, String answerName) {
        if (!x.contains(answerName)) {
            return null;
        }

        Instance instance = new DenseInstance(attributes.size());
        for (Feature feature : data.getFactors(Feature.class)) {
            instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));

        }

        if (data.getFactor(answerName) instanceof Class) {
            instance.setValue(attributes.get(answerName), x.get(answerName));
        } else {
            instance.setValue(attributes.get(answerName), x.getDouble(answerName));
        }

        return instance;
    }

    public Spreadsheet get() {
        return data;
    }

    @Required
    public void setDataDepot(DataDepot dataDepot) {
        this.dataDepot = dataDepot;
    }

}
