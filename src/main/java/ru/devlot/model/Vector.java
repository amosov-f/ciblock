package ru.devlot.model;

import javax.annotation.concurrent.Immutable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Immutable
public class Vector {

    protected final String id;

    protected final Map<String, String> name2value = new HashMap<>();

    public Vector(String id) {
        this.id = id;
    }

    public double getDouble(String name) {
        try {
            return NumberFormat.getInstance(Locale.FRANCE).parse(name2value.get(name)).doubleValue();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(name + " " + name2value.get(name) + " parse error!");
            throw new RuntimeException(e);
        }
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

    public static class ExpandingVector extends Vector {

        public ExpandingVector(String id) {
            super(id);
        }

        public void add(String name, String value) {
            name2value.put(name, value);
        }

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
