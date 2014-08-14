package ru.devlot.model;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Value {

    private final double value;

    private final int numInstances;
    private final double quality;

    public Value(double value, int numInstances, double quality) {
        this.value = value;
        this.numInstances = numInstances;
        this.quality = quality;
    }

    public Double getValue() {
        return value;
    }

    public int getNumInstances() {
        return numInstances;
    }

    public double getQuality() {
        return quality;
    }

}
