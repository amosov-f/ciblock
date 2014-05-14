package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.db.spreadsheet.DataDepot;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import ru.devlot.model.Factor.Answer;
import ru.devlot.model.Factor.Class;
import ru.devlot.model.Factor;
import ru.devlot.model.Factor.Regression;
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

    private DataDepot dataDepot;

    private Map<Integer, Classifier> classifiers;

    private Spreadsheet data;
    private List<Attribute> attributes;

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
                    attributes = getAttributes();
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
        for (int i : features.keySet()) {
            instance.setValue(attributes.get(i), features.get(i));
        }

        Instances test = new Instances("test", new ArrayList<>(attributes), 1);
        test.add(instance);

        Map<Integer, Double> answers = new HashMap<>();
        for (int answerIndex : classifiers.keySet()) {
            answers.put(answerIndex, classifiers.get(answerIndex).classifyInstance(instance));
        }

        return answers;
    }

    private Map<Integer, Classifier> train() throws Exception {
        Map<Integer, Classifier> classifiers = new HashMap<>();
        for (int i : data.getAnswers().keySet()) {
            Classifier classifier = train(i);
            classifiers.put(i, classifier);
            System.out.println(i + ": " + classifier);
            System.out.println(classifiers);
        }

        return classifiers;
    }


    private Classifier train(int answerIndex) throws Exception {
        Answer answer = data.getAnswers().get(answerIndex);

        Instances learn = new Instances(answer.getName(), new ArrayList<>(attributes), data.size());
        for (Vector x : data) {
            Instance instance = toInstance(x, answerIndex);
            if (instance != null) {
                learn.add(instance);
            }
        }
        learn.setClass(attributes.get(answerIndex));

        System.out.println(learn);

        Classifier classifier = type2classifier.get(answer.getClass()).getConstructor().newInstance();
        classifier.buildClassifier(learn);

        Evaluation evaluation = new Evaluation(learn);
        evaluation.crossValidateModel(classifier, learn, 3, new Random());
        System.out.println(evaluation.toSummaryString());

        return classifier;
    }

    private List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        for (Factor factor : data.getFactors()) {
            attributes.add(new Attribute(factor.getName()));
        }
        return attributes;
    }

    private Instance toInstance(Vector x, int answerIndex) {
        if (!x.contains(answerIndex)) {
            return null;
        }

        Instance instance = new DenseInstance(attributes.size());
        for (int i : data.getFeatures().keySet()) {
            instance.setValue(attributes.get(i), x.getDouble(i));

        }

        if (data.getFactor(answerIndex) instanceof Class) {
            instance.setValue(attributes.get(answerIndex), x.get(answerIndex));
        } else {
            instance.setValue(attributes.get(answerIndex), x.getDouble(answerIndex));
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
