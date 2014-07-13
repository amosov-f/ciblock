package ru.devlot.model;

public final class Info {

    private final String id;

    private final String ref;

    public Info(String id, String ref) {
        this.ref = ref;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

}
