package ru.devlot.db;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Required;
import ru.devlot.model.Factor;
import ru.devlot.model.Factor.Answer;
import ru.devlot.model.Factor.Class;
import ru.devlot.model.Factor.Regression;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Value;
import ru.devlot.model.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.LeastMedSq;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static ru.devlot.CiBlockServer.SLEEP_TIME;
import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.model.Factor.Feature;

@ThreadSafe
public class ClassifierDepot {

    private DataDepot dataDepot;

    private Spreadsheet data;

    private Map<String, Attribute> attributes;
    private Map<String, Classifier> classifiers;

    private Map<String, Integer> numsInstances;
    private Map<String, Double> qualities;

    private static final Map<
            java.lang.Class<? extends Answer>,
            List<java.lang.Class<? extends Classifier>>
    > TYPE_2_CLASSIFIERS = new HashMap<>();
    static {
        TYPE_2_CLASSIFIERS.put(Regression.class, Arrays.asList(LeastMedSq.class, SMOreg.class));
        TYPE_2_CLASSIFIERS.put(Class.ExpandingClass.class, Arrays.asList(BayesNet.class));
    }

    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    train();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (classifiers != null) {
                    System.out.println("classifiers: " + classifiers.values());
                }

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @NotNull
    public synchronized Map<String, Value> classify(Map<String, Double> features) throws Exception {
        Instance instance = new DenseInstance(features.size() + 1);
        for (String name : features.keySet()) {
            instance.setValue(attributes.get(name), features.get(name));
        }

        Instances instances = new Instances("", new ArrayList<>(), 1);
        instances.add(instance);

        Map<String, Value> answers = new HashMap<>();
        for (String name : classifiers.keySet()) {
            System.out.println(name);
            answers.put(name, new Value(
                    classifiers.get(name).classifyInstance(instance),
                    numsInstances.get(name),
                    qualities.get(name))
            );
        }

        return answers;
    }

    @NotNull
    public Spreadsheet get() {
        return data;
    }

    private void train() throws Exception {
        Spreadsheet data = dataDepot.get();

        List<Feature> features = data.getFeatures();

        Map<String, Attribute> attributes = new HashMap<>();
        for (Factor factor : data.getFactors()) {
            Attribute attribute;
            if (factor instanceof Class) {
                System.out.println(((Class) factor).getClasses());
                attribute = new Attribute(factor.getName(), ((Class) factor).getClasses());
            } else {
                attribute = new Attribute(factor.getName());
            }

            attributes.put(factor.getName(), attribute);
        }

        System.out.println("Train!");

        Map<String, Classifier> classifiers = new HashMap<>();
        Map<String, Integer> numsInstances = new HashMap<>();
        Map<String, Double> qualities = new HashMap<>();
        for (Answer answer : data.getAnswers()) {
            System.out.println(answer);

            Attribute answerAttribute = attributes.get(answer.getName());

            ArrayList<Attribute> answerAttributes = new ArrayList<>();
            for (Feature feature : features) {
                answerAttributes.add(attributes.get(feature.getName()));
            }
            answerAttributes.add(answerAttribute);

            Instances learn = new Instances(answer.getName(), answerAttributes, data.size());

            for (Vector x : data) {
                if (!x.contains(answer.getName())) {
                    continue;
                }

                Instance instance = new DenseInstance(answerAttributes.size());
                for (Feature feature : features) {
                    instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
                }

                if (answer instanceof Class) {
                    instance.setValue(answerAttribute, x.get(answer.getName()));
                } else {
                    instance.setValue(answerAttribute, x.getDouble(answer.getName()));
                }

                learn.add(instance);
            }
            learn.setClass(answerAttribute);


            Classifier classifier = TYPE_2_CLASSIFIERS.get(answer.getClass()).iterator().next().newInstance();
            double bestQuality = -1;

            for (java.lang.Class<? extends Classifier> classifierClass : TYPE_2_CLASSIFIERS.get(answer.getClass())) {
                Classifier curClassifier = classifierClass.newInstance();
                curClassifier.buildClassifier(learn);

                Evaluation evaluation = new Evaluation(learn);
                evaluation.crossValidateModel(curClassifier, learn, 5, new Random());

                double quality;
                if (answer instanceof Class) {
                    quality = evaluation.weightedFMeasure();
                } else {
                    quality = evaluation.correlationCoefficient();
                }

                if (quality > bestQuality) {
                    classifier = curClassifier;
                    bestQuality = quality;
                }
            }

            Evaluation evaluation = new Evaluation(learn);
            evaluation.crossValidateModel(classifier, learn, 5, new Random());

            System.out.println(evaluation.toSummaryString());

            classifier.buildClassifier(learn);

            classifiers.put(answer.getName(), classifier);
            numsInstances.put(answer.getName(), learn.size());
            qualities.put(answer.getName(), bestQuality);
        }

        synchronized (this) {
            this.data = data;
            this.attributes = attributes;
            this.classifiers = classifiers;
            this.numsInstances = numsInstances;
            this.qualities = qualities;
        }
    }

    @Required
    public void setDataDepot(DataDepot dataDepot) {
        this.dataDepot = dataDepot;
    }

}
