package ru.devlot.db;

import org.springframework.beans.factory.annotation.Required;
import ru.devlot.model.Factor;
import ru.devlot.model.Factor.Answer;
import ru.devlot.model.Factor.Class;
import ru.devlot.model.Factor.Regression;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LeastMedSq;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static ru.devlot.LotDeveloperEngine.SLEEP_TIME;
import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.model.Factor.Feature;

public class ClassifierDepot {

    private DataDepot dataDepot;

    private Map<String, Classifier> classifiers;

    private Spreadsheet data;
    private Map<String, Attribute> attributes;

    private static final Map<
            java.lang.Class<? extends Answer>,
            List<java.lang.Class<? extends Classifier>>
    > type2classifier = new HashMap<>();
    static {
        type2classifier.put(Regression.class, Arrays.asList(LeastMedSq.class, SMOreg.class));
        type2classifier.put(Class.class, Arrays.asList(SMO.class));
    }

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (ClassifierDepot.this) {
                    data = dataDepot.get();

                    try {
                        initAttributes();
                        train();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (classifiers != null) {
                        System.out.println("classifiers: " + classifiers.values());
                    }
                }

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized Map<String, Double> classify(Map<String, Double> features) throws Exception {
        Instance instance = new DenseInstance(getFeatures().size() + 1);
        for (String name : features.keySet()) {
            instance.setValue(attributes.get(name), features.get(name));
        }

        Map<String, Double> answers = new HashMap<>();
        for (String name : classifiers.keySet()) {
            answers.put(name, classifiers.get(name).classifyInstance(instance));
        }

        return answers;
    }

    private void train() throws Exception {
        System.out.println("Train!");

        classifiers = new HashMap<>();
        for (Answer answer : data.getFactors(Answer.class)) {
            train(answer);
        }
    }

    private void train(Answer answer) throws Exception {
        System.out.println(answer);

        Instances learn = new Instances(answer.getName(), getAttributes(answer.getName()), data.size());

        for (Vector x : data) {
            Instance instance = toInstance(x, answer.getName());
            if (instance != null) {
                learn.add(instance);
            }
        }
        learn.setClass(attributes.get(answer.getName()));

        Classifier classifier = null;
        double bestCorrelation = -1;

        for (java.lang.Class<? extends Classifier> classifierClass : type2classifier.get(answer.getClass())) {
            Classifier curClassifier = classifierClass.newInstance();
            curClassifier.buildClassifier(learn);

            Evaluation evaluation = new Evaluation(learn);
            evaluation.crossValidateModel(curClassifier, learn, 5, new Random());

            if (evaluation.correlationCoefficient() > bestCorrelation) {
                classifier = curClassifier;
                bestCorrelation = evaluation.correlationCoefficient();
            }
        }

        Evaluation evaluation = new Evaluation(learn);
        evaluation.crossValidateModel(classifier, learn, 5, new Random());

        System.out.println(evaluation.toSummaryString());

        classifiers.put(answer.getName(), classifier);
    }

    private void initAttributes() {
        attributes = new HashMap<>();
        for (Factor factor : data.getFactors()) {
            attributes.put(factor.getName(), new Attribute(factor.getName()));
        }
    }

    private ArrayList<Attribute> getAttributes(String answerName) {
        ArrayList<Attribute> attributes = new ArrayList<>(getFeatures());
        attributes.add(this.attributes.get(answerName));
        return attributes;
    }

    private List<Attribute> getFeatures() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (Feature feature : data.getFactors(Feature.class)) {
            attributes.add(this.attributes.get(feature.getName()));
        }
        return attributes;
    }

    private Instance toInstance(Vector x, String answerName) {
        if (!x.contains(answerName)) {
            return null;
        }

        Instance instance = new DenseInstance(getAttributes(answerName).size());
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
