package ru.spbu.astro.ciblock.commons;

import org.jetbrains.annotations.NotNull;

public final class CityBlockInfo {
    @NotNull
    private final String id;
    @NotNull
    private final String ref;

    public CityBlockInfo(@NotNull final String id, @NotNull final String ref) {
        this.id = id;
        this.ref = ref;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getRef() {
        return ref;
    }
}
