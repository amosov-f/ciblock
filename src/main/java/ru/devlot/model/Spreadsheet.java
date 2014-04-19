package ru.devlot.model;

import java.util.*;

import static ru.devlot.model.Factor.Type.ANSWER;
import static ru.devlot.model.Factor.Type.FEATURE;

public class Spreadsheet implements Iterable<Vector> {

    private final List<Factor> factors = new ArrayList<>();

    private final List<Vector> vectors = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public void add(Vector x) {
        vectors.add(x);
    }

    public Factor getFactor(int i) {
        return factors.get(i);
    }

    public Map<Integer, Factor> getFeatures() {
        Map<Integer, Factor> features = new HashMap<>();
        for (int i = 0; i < factors.size(); ++i) {
            if (factors.get(i).type == FEATURE) {
                features.put(i, factors.get(i));
            }
        }
        return features;
    }

    public Map<Integer, Factor> getAnswers() {
        Map<Integer, Factor> answers = new HashMap<>();
        for (int i = 0; i < factors.size(); ++i) {
            if (factors.get(i).type == ANSWER) {
                answers.put(i, factors.get(i));
            }
        }
        return answers;
    }

    public int size() {
        return vectors.size();
    }

    @Override
    public String toString() {
        String s = "name\t";
        for (Factor factor : factors) {
            s += factor + "\t";
        }
        s += "\n";
        for (Vector x : vectors) {
            s += x + "\n";
        }
        return s;
    }

    @Override
    public Iterator<Vector> iterator() {
        return vectors.iterator();
    }
}
