package ru.spbu.astro.ciblock.commons;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Vector {
    @NotNull
    private final String id;
    @NotNull
    private final Map<String, String> values = new HashMap<>();

    public Vector(@NotNull final String id) {
        this.id = id;
    }

    public double getDouble(@NotNull final String name) {
        try {
            return NumberFormat.getInstance(Locale.FRANCE).parse(values.get(name)).doubleValue();
        } catch (ParseException | NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public String get(@NotNull final String name) {
        return values.get(name);
    }

    public boolean contains(@NotNull final String name) {
        return values.get(name) != null && !values.get(name).isEmpty();
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void add(@NotNull final String name, @Nullable final String value) {
        values.put(name, value);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Vector vector = (Vector) o;
        return new EqualsBuilder().append(id, vector.id).append(values, vector.values).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(values).toHashCode();
    }

    @Override
    public String toString() {
        return Joiner.on('\t').join(id, values.values());
    }
}
