package ru.devlot.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Factor {

    public final String name;

    public final String dimension;

    public final Type type;

    public Factor(String description) {
        int start = 0;
        if (description.startsWith("#")) {
            type = Type.ANSWER;
            start = 1;
        } else {
            type = Type.FEATURE;
        }

        Matcher matcher = Pattern.compile("\\[.+\\]").matcher(description);
        int end = description.length();
        if (matcher.find()) {
            String s = matcher.group();
            this.dimension = s.substring(1, s.length() - 1).trim();
            end = matcher.start();
        } else {
            this.dimension = null;
        }

        name = description.substring(start, end).trim();
    }

    public static enum Type {
        FEATURE, ANSWER
    }

    @Override
    public String toString() {
        return (type == Type.ANSWER ? "#" : "") + name + " [" + dimension + "]";
    }
}
