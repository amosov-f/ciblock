package ru.spbu.astro.ciblock.provider;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Factor;
import ru.spbu.astro.ciblock.commons.Vector;
import ru.spbu.astro.ciblock.commons.Worksheet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: amosov-f
 * Date: 30.05.15
 * Time: 16:39
 */
public final class FileProvider extends AbstractWorksheetProvider {
    @Inject
    public FileProvider(@NotNull final Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public Worksheet uploadImpl(@NotNull final String worksheetId) throws IOException {
        final Worksheet worksheet = new Worksheet();
        final String[][] table = ((List<String>) FileUtils.readLines(new File(worksheetId))).stream()
                .map(line -> line.split("\t"))
                .toArray(String[][]::new);

        final List<String> names = new ArrayList<>();
        final List<Factor.Class> classes = new ArrayList<>();
        for (final String title : Arrays.copyOfRange(table[0], 1, table[0].length)) {
            if (!isCommented(title)) {
                final Factor factor = Factor.parse(title);
                worksheet.addFactor(factor);
                if (factor instanceof Factor.Class) {
                    classes.add((Factor.Class) factor);
                }
                names.add(factor.getName());
            } else {
                names.add(null);
            }
        }

        for (final String[] row : Arrays.copyOfRange(table, 1, table.length)) {
            final Vector x = new Vector(row[0]);
            for (int i = 1; i < row.length; i++) {
                final String value = row[i];
                Optional.ofNullable(names.get(i - 1)).ifPresent(name -> x.add(name, commented(value)));
            }
            worksheet.add(x);
            classes.forEach(clazz -> Optional.ofNullable(x.get(clazz.getName())).ifPresent(clazz::addValue));
        }

        return worksheet;
    }

    @NotNull
    @Override
    public String getName() {
        return "file";
    }
}
