package ru.spbu.astro.ciblock.provider;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Factor;
import ru.spbu.astro.ciblock.commons.Vector;
import ru.spbu.astro.ciblock.commons.Worksheet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

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
    public Worksheet uploadImpl(@NotNull final String worksheetId) throws IOException {
        final Worksheet worksheet = new Worksheet();
        final String[][] table = ((List<String>) FileUtils.readLines(new File(worksheetId))).stream()
                .map(line -> line.split("\t"))
                .toArray(String[][]::new);

        final List<Factor.Class> classes = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        final String[] titles = table[0];
        for (int i = 1; i < titles.length; i++) {
            if (!isCommented(titles[i])) {
                final Factor factor = Factor.parse(titles[i]);
                worksheet.addFactor(factor);
                if (factor instanceof Factor.Class) {
                    classes.add((Factor.Class) factor);
                }
                names.add(factor.getName());
            } else {
                names.add(null);
            }
        }

        for (int i = 1; i < table.length; i++) {
            final String[] row = table[i];
            final Vector x = new Vector(row[0]);
            for (int j = 1; j < row.length; ++j) {
                final String value = row[j];
                Optional.ofNullable(names.get(j - 1)).ifPresent(name -> x.add(name, commented(value)));
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
