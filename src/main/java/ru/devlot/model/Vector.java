package ru.devlot.model;

import java.util.ArrayList;
import java.util.List;

public class Vector {

    public final String name;

    private final List<Double> values = new ArrayList<>();


    public Vector(String name) {
        this.name = name;
    }

    public void add(double value) {
        values.add(value);
    }

    public double get(int i) {
        return values.get(i);
    }

    @Override
    public String toString() {
        String s = name + "\t";
        for (double value : values) {
            s += value + "\t";
        }
        return s;
    }
}
