package ru.devlot.model;

import java.util.HashMap;
import java.util.Map;

public class Vector {

    private final String id;

    private final Map<String, String> name2value = new HashMap<>();

    public Vector(String id) {
        this.id = id;
    }

    public void add(String name, String value) {
        name2value.put(name, value);
    }

    public double getDouble(String name) {
        return new Double(name2value.get(name));
    }

    public String get(String name) {
        return name2value.get(name);
    }

    public boolean contains(String name) {
        return name2value.get(name) != null && !name2value.get(name).isEmpty();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        String s = id + "\t";
        for (String value : name2value.values()) {
            s += value + "\t";
        }
        return s;
    }

}
