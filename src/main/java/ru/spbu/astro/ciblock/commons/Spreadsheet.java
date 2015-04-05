package ru.spbu.astro.ciblock.commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 06.04.15
 * Time: 0:51
 */
public final class Spreadsheet {
    @NotNull
    private final Map<String, Worksheet> worksheets = new HashMap<>();

    public void add(@NotNull final String id, @NotNull final Worksheet worksheet) {
        worksheets.put(id, worksheet);
    }

    @NotNull
    public Worksheet get(@NotNull final String id) {
        return worksheets.get(id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return new EqualsBuilder().append(worksheets, ((Spreadsheet) o).worksheets).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(worksheets).toHashCode();
    }
}
