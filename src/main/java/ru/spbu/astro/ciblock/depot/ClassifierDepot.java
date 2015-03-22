package ru.spbu.astro.ciblock.depot;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.*;
import ru.spbu.astro.ciblock.commons.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.LeastMedSq;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.spbu.astro.ciblock.depot.SpreadsheetDepot.DataDepot;

@Singleton
@ThreadSafe
public final class ClassifierDepot implements Supplier<Spreadsheet> {
    private static final Logger LOG = Logger.getLogger(ClassifierDepot.class.getName());

    private Map<String, ClassifierMeta> metas;
    private Map<String, Attribute> attributes;

    private volatile Spreadsheet data;
    
    @NotNull
    private final DataDepot dataDepot;

    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private static final Map<Class<? extends Factor.Answer>, List<Class<? extends Classifier>>> CLASSIFIERS 
            = new HashMap<Class<? extends Factor.Answer>, List<Class<? extends Classifier>>>() {{
        put(Factor.Regression.class, Arrays.asList(LeastMedSq.class, SMOreg.class, MultilayerPerceptron.class));
        put(Factor.Class.class, Arrays.asList(BayesNet.class, SMO.class));
    }};

    @Inject
    public ClassifierDepot(@NotNull final DataDepot dataDepot, @NotNull final Properties properties) {
        this.dataDepot = dataDepot;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (train()) {
                    LOG.info("Classifiers training completed");
                } else {
                    LOG.info("Spreadsheet hasn't changed");
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Training error!", e);
            }
        }, 0, Long.parseLong(properties.getProperty("ciblock.classifier.retry_delay")), TimeUnit.SECONDS);
    }

    @NotNull
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public Map<String, Value> classify(@NotNull final Map<String, Double> features) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        final Map<String, ClassifierMeta> metas;
        final Map<String, Attribute> attributes;
        synchronized (this) {
            metas = this.metas;
            attributes = this.attributes;
        }
        
        final Instance instance = new DenseInstance(features.size() + 1);
        for (final String name : features.keySet()) {
            instance.setValue(attributes.get(name), features.get(name));
        }

        final Map<String, Value> answers = new HashMap<>();
        for (final String name : metas.keySet()) {
            final ClassifierMeta meta = metas.get(name);
            instance.setDataset(meta.getDataset());
            try {
                answers.put(name, new Value(
                        meta.getClassifier().classifyInstance(instance),
                        meta.getDataset().size(),
                        meta.getQuality()
                ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return answers;
    }

    @NotNull
    @Override
    public Spreadsheet get() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return data;
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    private boolean train() throws Exception {
        final Spreadsheet data = dataDepot.get();
        if (data.equals(this.data)) {
            return false;
        }

        final Map<String, Attribute> attributes = new HashMap<>();
        for (final Factor factor : data.getFactors()) {
            final Attribute attribute;
            if (factor instanceof Factor.Class) {
                attribute = new Attribute(factor.getName(), ((Factor.Class) factor).getClasses());
            } else {
                attribute = new Attribute(factor.getName());
            }

            attributes.put(factor.getName(), attribute);
        }

        LOG.info("Training...");

        final Factor.Feature[] features = data.getFeatures();
        final Map<String, ClassifierMeta> metas = new HashMap<>();
        for (final Factor.Answer answer : data.getAnswers()) {
            final Attribute answerAttribute = attributes.get(answer.getName());

            final ArrayList<Attribute> answerAttributes = new ArrayList<>();
            for (final Factor.Feature feature : features) {
                answerAttributes.add(attributes.get(feature.getName()));
            }
            answerAttributes.add(answerAttribute);

            final Instances dataset = new Instances(answer.getName(), answerAttributes, data.size());

            for (final Vector x : data.getVectors()) {
                if (!x.contains(answer.getName())) {
                    continue;
                }

                final Instance instance = new DenseInstance(answerAttributes.size());
                for (final Factor.Feature feature : features) {
                    instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
                }

                if (answer instanceof Factor.Class) {
                    instance.setValue(answerAttribute, x.get(answer.getName()));
                } else {
                    instance.setValue(answerAttribute, x.getDouble(answer.getName()));
                }

                dataset.add(instance);
            }
            dataset.setClass(answerAttribute);
            
            Classifier bestClassifier = CLASSIFIERS.get(answer.getClass()).iterator().next().newInstance();
            double bestQuality = -1;
            for (final Class<? extends Classifier> classifierClass : CLASSIFIERS.get(answer.getClass())) {
                final Classifier classifier = classifierClass.newInstance();
                classifier.buildClassifier(dataset);

                final Evaluation evaluation = new Evaluation(dataset);
                evaluation.crossValidateModel(classifier, dataset, 5, new Random(0));

                final double quality;
                if (answer instanceof Factor.Class) {
                    quality = evaluation.weightedFMeasure();
                } else {
                    quality = evaluation.correlationCoefficient();
                }

                System.out.println("classifier: " + classifier);
                System.out.println("evaluation: " + evaluation.toSummaryString());

                if (quality > bestQuality) {
                    bestClassifier = classifier;
                    bestQuality = quality;
                }
            }

            System.out.println("best: " + bestClassifier);

            System.out.println();
            System.out.println("#######################################################");
            System.out.println();

            final Evaluation evaluation = new Evaluation(dataset);
            evaluation.crossValidateModel(bestClassifier, dataset, 5, new Random());
            LOG.fine(evaluation.toSummaryString());
            
            bestClassifier.buildClassifier(dataset);
            
            metas.put(answer.getName(), new ClassifierMeta(bestClassifier, dataset, bestQuality));
        }

        synchronized (this) {
            this.metas = metas;
            this.attributes = attributes;
            this.data = data;
        }
        latch.countDown();
        return true;
    }
}
