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
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
@ThreadSafe
public final class ModelDepot {
    private static final Logger LOG = Logger.getLogger(ModelDepot.class.getName());

    private static final String REFERENCE = "ссылка";

    private Map<String, Attribute> attributes;
    private Map<String, ClassifierMeta> metas;
    private NearestNeighbourSearch search;

    private volatile Spreadsheet spreadsheet;
    
    @NotNull
    private final SpreadsheetDepot spreadsheetDepot;

    @NotNull
    private final ExecutorService executor = Executors.newScheduledThreadPool(10);

    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private static final Map<Class<? extends Factor.Answer>, List<Class<? extends Classifier>>> CLASSIFIERS 
            = new HashMap<Class<? extends Factor.Answer>, List<Class<? extends Classifier>>>() {{
        put(Factor.Regression.class, Arrays.asList(LeastMedSq.class, SMOreg.class, MultilayerPerceptron.class));
        put(Factor.Class.class, Arrays.asList(BayesNet.class, SMO.class));
    }};

    @Inject
    public ModelDepot(@NotNull final SpreadsheetDepot spreadsheetDepot, @NotNull final Properties properties) {
        this.spreadsheetDepot = spreadsheetDepot;
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
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public CityBlockInfo[] getKNearestNeighbours(@NotNull final Map<String, Double> features, final int k) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        final NearestNeighbourSearch search;
        final Map<String, Attribute> attributes;
        final Spreadsheet spreadsheet;
        synchronized (this) {
            search = this.search;
            attributes = this.attributes;
            spreadsheet = this.spreadsheet;
        }

        final Instance instance = new DenseInstance(features.size() + 1);
        for (final Factor.Feature feature : spreadsheet.get(SpreadsheetDepot.DATA).getFeatures()) {
            instance.setValue(attributes.get(feature.getName()), features.get(feature.getName()));
        }

        final List<CityBlockInfo> neighbours = new ArrayList<>();
        try {
            for (final Instance neighbour : search.kNearestNeighbours(instance, k)) {
                final String id = neighbour.stringValue(attributes.get(REFERENCE));
                final String ref = spreadsheet.get(SpreadsheetDepot.INFO).get(id).get(REFERENCE);
                if (ref != null) {
                    neighbours.add(new CityBlockInfo(id, ref));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return neighbours.toArray(new CityBlockInfo[neighbours.size()]);
    }

    @NotNull
    public Spreadsheet getSpreadsheet() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return spreadsheet;
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    private boolean train() throws Exception {
        final Spreadsheet spreadsheet = spreadsheetDepot.get();
        if (spreadsheet.equals(this.spreadsheet)) {
            return false;
        }

        final Worksheet data = spreadsheet.get(SpreadsheetDepot.DATA);
        final Map<String, Attribute> attributes = attributes(data);
        LOG.info("Training...");
        final Map<String, Future<ClassifierMeta>> metaFutures = new HashMap<>();
        for (final Factor.Answer answer : data.getAnswers())
            metaFutures.put(answer.getName(), executor.submit(new Callable<ClassifierMeta>() {
                @Override
                public ClassifierMeta call() throws Exception {
                    final Instances dataset = dataset(
                            spreadsheet,
                            attributes,
                            answer.getName(),
                            answer instanceof Factor.Class ? (instance, answerAttribute, x) -> instance.setValue(answerAttribute, x.get(answerAttribute.name()))
                                                           : (instance, answerAttribute, x) -> instance.setValue(answerAttribute, x.getDouble(answerAttribute.name()))

                    );

                    final List<ClassifierMeta> metas = new ArrayList<>();
                    for (final Class<? extends Classifier> classifierClass : CLASSIFIERS.get(answer.getClass())) {
                        metas.add(train(classifierClass.newInstance(), dataset, answer instanceof Factor.Class));
                    }

                    final ClassifierMeta bestClassifierMeta = Collections.min(metas);
                    final Classifier classifier = bestClassifierMeta.getClassifier();
                    classifier.buildClassifier(dataset);
                    LOG.info("Best classifier: " + bestClassifierMeta.getClassifier());
                    return bestClassifierMeta;
                }
            }));
        final Future<NearestNeighbourSearch> searchFuture = executor.submit(new Callable<NearestNeighbourSearch>() {
            @Override
            public NearestNeighbourSearch call() throws Exception {
                return new LinearNNSearch(dataset(
                        spreadsheet,
                        attributes,
                        REFERENCE,
                        (instance, answerAttribute, x) -> instance.setValue(answerAttribute, x.getId())
                ));
            }
        });

        final Map<String, ClassifierMeta> metas = new HashMap<>();
        for (final String name : metaFutures.keySet()) {
            metas.put(name, metaFutures.get(name).get());
        }
        final NearestNeighbourSearch search = searchFuture.get();

        synchronized (this) {
            this.spreadsheet = spreadsheet;
            this.attributes = attributes;
            this.metas = metas;
            this.search = search;
        }
        latch.countDown();
        return true;
    }

    @NotNull
    private static Map<String, Attribute> attributes(@NotNull final Worksheet data) {
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
        attributes.put(REFERENCE, new Attribute(REFERENCE, (List<String>) null));
        return attributes;
    }

    @NotNull
    private static Instances dataset(@NotNull final Spreadsheet spreadsheet,
                                     @NotNull final Map<String, Attribute> attributes,
                                     @NotNull final String answerName,
                                     @NotNull final Model model)
    {
        final Worksheet data = spreadsheet.get(SpreadsheetDepot.DATA);
        final Worksheet info = spreadsheet.get(SpreadsheetDepot.INFO);
        final Factor.Feature[] features = data.getFeatures();

        final Attribute answerAttribute = attributes.get(answerName);

        final ArrayList<Attribute> answerAttributes = new ArrayList<>();
        for (final Factor.Feature feature : features) {
            answerAttributes.add(attributes.get(feature.getName()));
        }
        answerAttributes.add(answerAttribute);

        final Instances dataset = new Instances(answerName, answerAttributes, data.size());

        for (final Vector x : data.getVectors()) {
            if (!x.contains(answerName) && !info.get(x.getId()).contains(answerName)) {
                continue;
            }

            final Instance instance = new DenseInstance(answerAttributes.size());
            for (final Factor.Feature feature : features) {
                instance.setValue(attributes.get(feature.getName()), x.getDouble(feature.getName()));
            }

            model.setAnswerValue(instance, answerAttribute, x);

            dataset.add(instance);
        }
        dataset.setClass(answerAttribute);
        return dataset;
    }

    @NotNull
    private static ClassifierMeta train(@NotNull final Classifier classifier,
                                        @NotNull final Instances dataset,
                                        final boolean fMeasure) throws Exception
    {
        classifier.buildClassifier(dataset);
        final Evaluation evaluation = new Evaluation(dataset);
        evaluation.crossValidateModel(classifier, dataset, 5, new Random(0));
        final double quality = fMeasure ? evaluation.weightedFMeasure() : evaluation.correlationCoefficient();

        LOG.info("Classifier: " + classifier);
        LOG.info("Evaluation: " + evaluation.toSummaryString());

        return new ClassifierMeta(classifier, dataset, quality);
    }

    public interface Model {
        void setAnswerValue(@NotNull final Instance instance, @NotNull Attribute answerAttribute, @NotNull final Vector x);
    }
}
