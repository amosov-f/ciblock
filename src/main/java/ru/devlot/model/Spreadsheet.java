package ru.devlot.model;

import javax.annotation.concurrent.Immutable;
import java.util.*;

import static ru.devlot.model.Factor.Answer;
import static ru.devlot.model.Factor.Feature;

@Immutable
public class Spreadsheet implements Iterable<Vector> {

    protected final Map<String, Factor> name2factor = new HashMap<>();

    protected final Map<String, Vector> id2vector = new HashMap<>();

    public Vector get(String id) {
        for (Vector x : this) {
            if (x.getId().equals(id)) {
                return x;
            }
        }
        return null;
    }

    private <T extends Factor> List<T> getFactors(java.lang.Class<T> clazz) {
        List<T> features = new ArrayList<>();
        for (Factor factor : name2factor.values()) {
            if (clazz.isInstance(factor)) {
                features.add(clazz.cast(factor));
            }
        }
        return features;
    }

    public List<Factor> getFactors() {
        return getFactors(Factor.class);
    }

    public List<Feature> getFeatures() {
        return getFactors(Feature.class);
    }

    public List<Answer> getAnswers() {
        return getFactors(Answer.class);
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

    public static class ExpandingSpreadsheet extends Spreadsheet {

        public void addFactor(Factor factor) {
            name2factor.put(factor.getName(), factor);
        }

        public void add(Vector x) {
            id2vector.put(x.getId(), x);
        }

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
