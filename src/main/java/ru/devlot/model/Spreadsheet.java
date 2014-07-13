package ru.devlot.model;

import java.util.*;

public final class Spreadsheet implements Iterable<Vector> {

    private final Map<String, Factor> name2factor = new HashMap<>();

    private final Map<String, Vector> id2vector = new HashMap<>();

    public void addFactor(Factor factor) {
        name2factor.put(factor.getName(), factor);
    }

    public void add(Vector x) {
        id2vector.put(x.getId(), x);
    }

    public Factor getFactor(String name) {
        return name2factor.get(name);
    }

    public Vector get(String id) {
        for (Vector x : this) {
            if (x.getId().equals(id)) {
                return x;
            }
        }
        return null;
    }

    public <T extends Factor> List<T> getFactors(java.lang.Class<T> type) {
        List<T> features = new ArrayList<>();
        for (Factor factor : name2factor.values()) {
            if (type.isInstance(factor)) {
                features.add((T) factor);
            }
        }
        return features;
    }

    public List<Factor> getFactors() {
        return getFactors(Factor.class);
    }

    public int size() {
        return id2vector.size();
    }

    public List<Double> getDoubles(String name) {
        if (!name2factor.containsKey(name)) {
            return null;
        }

        List<Double> values = new ArrayList<>();
        for (Vector x : this) {
            values.add(x.getDouble(name));
        }
        return values;
    }

    @Override
    public String toString() {
        String s = "name\t";
        for (Factor factor : name2factor.values()) {
            s += factor.toString() + "\t";
        }
        s += "\n";
        for (Vector x : id2vector.values()) {
            s += x + "\n";
        }
        return s;
    }

    @Override
    public Iterator<Vector> iterator() {
        return id2vector.values().iterator();
    }

}
