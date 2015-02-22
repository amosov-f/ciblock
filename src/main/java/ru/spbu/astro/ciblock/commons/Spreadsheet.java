package ru.spbu.astro.ciblock.commons;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Spreadsheet {
    @NotNull
    private final List<Factor> factors = new ArrayList<>();
    @NotNull
    private final Map<String, Vector> vectors = new HashMap<>();

    @NotNull
    public Vector get(@NotNull final String id) {
        return vectors.get(id);
    }

    @NotNull
    public List<Vector> getVectors() {
        return new ArrayList<>(vectors.values());
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <T extends Factor> List<T> getFactors(@NotNull final Class<T> clazz) {
        return factors.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }

    @NotNull
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public Factor[] getFactors() {
        final List<Factor> factors = getFactors(Factor.class); 
        return factors.toArray(new Factor[factors.size()]);
    }

    @NotNull
    public Factor.Feature[] getFeatures() {
        final List<Factor.Feature> features = getFactors(Factor.Feature.class);
        return features.toArray(new Factor.Feature[features.size()]);
    }

    @NotNull
    public Factor.Answer[] getAnswers() {
        final List<Factor.Answer> answers = getFactors(Factor.Answer.class);
        return answers.toArray(new Factor.Answer[answers.size()]);
    }

    public int size() {
        return vectors.size();
    }

    @NotNull
    public double[] getDoubles(@NotNull final String name) {
        return vectors.values().stream().mapToDouble(x -> x.getDouble(name)).toArray();
    }

    public void addFactor(@NotNull final Factor factor) {
        factors.add(factor);
    }

    public void add(@NotNull final Vector x) {
        vectors.put(x.getId(), x);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Spreadsheet spreadsheet = (Spreadsheet) o;
        return new EqualsBuilder().append(factors, spreadsheet.factors).append(vectors, spreadsheet.vectors).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(factors).append(vectors).toHashCode();
    }
    
    @Override
    public String toString() {
        return "name\t" + Joiner.on('\t').join(factors) + "\n" + Joiner.on('\n').join(vectors.values());
    }
}
