package ru.devlot.model;

import java.util.ArrayList;
import java.util.List;

public class Vector {

    private final String id;

    private final List<String> values = new ArrayList<>();

    public Vector(String id) {
        this.id = id;
    }

    public void add(String value) {
        values.add(value);
    }

    public double getDouble(int i) {
        return new Double(values.get(i));
    }

    public String get(int i) {
        return values.get(i);
    }

    public boolean contains(int i) {
        return values.get(i) != null && !values.get(i).isEmpty();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        String s = id + "\t";
        for (String value : values) {
            s += value + "\t";
        }
        return s;
    }

}
