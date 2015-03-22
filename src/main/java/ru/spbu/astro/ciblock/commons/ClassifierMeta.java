package ru.spbu.astro.ciblock.commons;

import org.jetbrains.annotations.NotNull;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * User: amosov-f
 * Date: 22.02.15
 * Time: 22:54
 */
public final class ClassifierMeta {
    @NotNull
    private final Classifier classifier;
    @NotNull
    private final Instances dataset;
    private final double quality;

    public ClassifierMeta(@NotNull final Classifier classifier,
                          @NotNull final Instances dataset,
                          final double quality) 
    {
        this.classifier = classifier;
        this.dataset = dataset;
        this.quality = quality;
    }

    @NotNull
    public Classifier getClassifier() {
        return classifier;
    }

    @NotNull
    public Instances getDataset() {
        return dataset;
    }

    public double getQuality() {
        return quality;
    }
}
