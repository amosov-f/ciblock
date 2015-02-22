package ru.spbu.astro.ciblock.commons;

public final class Value {
    private final double value;
    private final int numInstances;
    private final double quality;

    public Value(final double value, final int numInstances, final double quality) {
        this.value = value;
        this.numInstances = numInstances;
        this.quality = quality;
    }

    public double getValue() {
        return value;
    }

    public int getNumInstances() {
        return numInstances;
    }

    public double getQuality() {
        return quality;
    }
}
