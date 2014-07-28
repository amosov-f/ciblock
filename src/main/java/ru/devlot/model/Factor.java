package ru.devlot.model;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
public class Factor {

    private final String name;

    private final String dimension;

    private static final String REGRESSION_PREFIX = "$";
    private static final String CLASS_PREFIX = "#";

    protected Factor(String name, String dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public String getName() {
        return name;
    }

    public String getDimension() {
        return dimension;
    }

    public static Factor parse(String description) {
        int start = 0;
        if (description.startsWith(REGRESSION_PREFIX)) {
            start = REGRESSION_PREFIX.length();
        }
        if (description.startsWith(CLASS_PREFIX)) {
            start = CLASS_PREFIX.length();
        }

        String dimension = null;

        Matcher matcher = Pattern.compile("\\[.+\\]").matcher(description);
        int end = description.length();
        if (matcher.find()) {
            String s = matcher.group();
            dimension = s.substring(1, s.length() - 1).trim();
            end = matcher.start();
        }

        String name = description.substring(start, end).trim();

        Factor factor = new Feature(name, dimension);
        if (description.startsWith(REGRESSION_PREFIX)) {
            factor = new Regression(name, dimension);
        }
        if (description.startsWith(CLASS_PREFIX)) {
            factor = new Class(name, dimension);
        }

        return factor;
    }

    @Override
    public String toString() {
        if (dimension == null) {
            return name;
        }
        return name + " [" + dimension + "]";
    }

    @Immutable
    public static final class Feature extends Factor {

        protected Feature(String name, String dimension) {
            super(name, dimension);
        }

    }

    @Immutable
    public static class Answer extends Factor {

        protected Answer(String name, String dimension) {
            super(name, dimension);
        }

    }

    @Immutable
    public static final class Regression extends Answer {

        protected Regression(String name, String dimension) {
            super(name, dimension);
        }

        @Override
        public String toString() {
            return "$" + super.toString();
        }

    }

    public static final class Class extends Answer {

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

}
