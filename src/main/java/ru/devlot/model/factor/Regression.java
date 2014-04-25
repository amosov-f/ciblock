package ru.devlot.model.factor;

public class Regression extends Answer {

    protected Regression(String name, String dimension) {
        super(name, dimension);
    }

    @Override
    public String toString() {
        return "$" + super.toString();
    }
}
