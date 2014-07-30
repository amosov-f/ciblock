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

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static ru.devlot.LotDeveloperEngine.SLEEP_TIME;
import static ru.devlot.db.SpreadsheetDepot.DataDepot;
import static ru.devlot.model.Factor.Feature;

@ThreadSafe
public class ClassifierDepot {

    private DataDepot dataDepot;

    private Spreadsheet data;

    private Map<String, Attribute> attributes;
    private Map<String, Classifier> classifiers;

    private static final Map<
            java.lang.Class<? extends Answer>,
            List<java.lang.Class<? extends Classifier>>
    > type2classifiers = new HashMap<>();
    static {
        type2classifiers.put(Regression.class, Arrays.asList(LeastMedSq.class, SMOreg.class));
        type2classifiers.put(Class.class, Arrays.asList(SMO.class));
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

    public Map<String, Double> classify(Map<String, Double> features) throws Exception {
        Instance instance = new DenseInstance(features.size() + 1);
        for (String name : features.keySet()) {
            instance.setValue(attributes.get(name), features.get(name));
        }

        Map<String, Double> answers = new HashMap<>();
        for (String name : classifiers.keySet()) {
            answers.put(name, classifiers.get(name).classifyInstance(instance));
        }

        return answers;
    }

    public Spreadsheet get() {
        return data;
    }

    private void train() throws Exception {
        Spreadsheet data = dataDepot.get();

        List<Feature> features = data.getFactors(Feature.class);

        Map<String, Attribute> attributes = new HashMap<>();
        for (Factor factor : data.getFactors()) {
            attributes.put(factor.getName(), new Attribute(factor.getName()));
        }

        System.out.println("Train!");

        Map<String, Classifier> classifiers = new HashMap<>();
        for (Answer answer : data.getFactors(Answer.class)) {
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


            Classifier classifier = null;
            double bestCorrelation = -1;

            for (java.lang.Class<? extends Classifier> classifierClass : type2classifiers.get(answer.getClass())) {
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

        synchronized (this) {
            this.data = data;
            this.attributes = attributes;
            this.classifiers = classifiers;
        }
    }

    @Required
    public void setDataDepot(DataDepot dataDepot) {
        this.dataDepot = dataDepot;
    }

}
