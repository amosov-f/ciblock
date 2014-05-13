package ru.devlot.model.factor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Class extends Answer {

    private final Set<String> classes = new HashSet<>();

    protected Class(String name, String dimension) {
        super(name, dimension);
    }

    public void add(String newClass) {
        classes.add(newClass);
    }

    public List<String> getClasses() {
        return new ArrayList<>(classes);
    }

    @Override
    public String toString() {
        return "#" + super.toString();
    }

}
