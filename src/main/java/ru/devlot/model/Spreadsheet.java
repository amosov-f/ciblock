package ru.devlot.model;

import ru.devlot.model.factor.Answer;
import ru.devlot.model.factor.Factor;
import ru.devlot.model.factor.Feature;
import ru.devlot.model.factor.Class;

import java.util.*;

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

    public Map<Integer, Feature> getFeatures() {
        Map<Integer, Feature> features = new HashMap<>();
        for (int i = 0; i < factors.size(); ++i) {
            if (factors.get(i) instanceof Feature) {
                features.put(i, (Feature) factors.get(i));
            }
        }
        return features;
    }

    public Map<Integer, Answer> getAnswers() {
        Map<Integer, Answer> answers = new HashMap<>();
        for (int i = 0; i < factors.size(); ++i) {
            if (factors.get(i) instanceof Answer) {
                answers.put(i, (Answer) factors.get(i));
            }
        }
        return answers;
    }

    public Map<Integer, Class> getClasses() {
        Map<Integer, Class> answers = new HashMap<>();
        for (int i = 0; i < factors.size(); ++i) {
            if (factors.get(i) instanceof Class) {
                answers.put(i, (Class) factors.get(i));
            }
        }
        return answers;
    }

    public List<Factor> getFactors() {
        return factors;
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
