package ru.spbu.astro.ciblock.commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Factor {
    @NotNull
    private final String name;
    @Nullable
    private final String dimension;

    protected Factor(@NotNull final String name, @Nullable final String dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    @NotNull
    public static Factor parse(@NotNull final String description) {
        int start = 0;
        if (description.startsWith(Regression.PREFIX)) {
            start = Regression.PREFIX.length();
        }
        if (description.startsWith(Class.PREFIX)) {
            start = Class.PREFIX.length();
        }

        String dimension = null;
        final Matcher matcher = Pattern.compile("\\[.+\\]").matcher(description);
        int end = description.length();
        if (matcher.find()) {
            final String s = matcher.group();
            dimension = s.substring(1, s.length() - 1).trim();
            end = matcher.start();
        }

        final String name = description.substring(start, end).trim();

        if (description.startsWith(Regression.PREFIX)) {
            return new Regression(name, dimension);
        }
        if (description.startsWith(Class.PREFIX)) {
            return new Factor.Class(name, dimension);
        }
        return new Feature(name, dimension);
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @Nullable
    public final String getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Factor factor = (Factor) o;
        return new EqualsBuilder().append(name, factor.name).append(dimension, factor.dimension).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(dimension).toHashCode();
    }

    @Override
    public String toString() {
        return dimension != null ? name + " [" + dimension + "]" : name;
    }

    public static final class Feature extends Factor {
        private Feature(@NotNull final String name, @Nullable final String dimension) {
            super(name, dimension);
        }
    }

    public static class Answer extends Factor {
        protected Answer(@NotNull final String name, @Nullable final String dimension) {
            super(name, dimension);
        }
    }

    public static final class Regression extends Answer {
        private static final String PREFIX = "$";
        
        private Regression(@NotNull final String name, @Nullable final String dimension) {
            super(name, dimension);
        }

        @Override
        public String toString() {
            return PREFIX + super.toString();
        }
    }

    public static final class Class extends Answer {
        private static final String PREFIX = "#";
        
        @NotNull
        private final Set<String> classes = new HashSet<>();

        private Class(@NotNull final String name, @Nullable final String dimension) {
            super(name, dimension);
        }

        @NotNull
        public List<String> getClasses() {
            return new ArrayList<>(classes);
        }

        public void addValue(@NotNull final String value) {
            classes.add(value);
        }

        @Override
        public String toString() {
            return "#" + super.toString();
        }
    }
}
