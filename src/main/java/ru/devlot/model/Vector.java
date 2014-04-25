package ru.devlot.model;

import java.util.ArrayList;
import java.util.List;

public class Vector {

    private final String name;

    private final List<String> values = new ArrayList<>();

    public Vector(String name) {
        this.name = name;
    }

    public void add(String value) {
        values.add(value);
    }

    public String getName() {
        return name;
    }

    public double getDouble(int i) {
        return new Double(values.get(i));
    }

    public String get(int i) {
        return values.get(i);
    }

    @Override
    public String toString() {
        String s = name + "\t";
        for (String value : values) {
            s += value + "\t";
        }
        return s;
    }
}
