package ru.spbu.astro.ciblock.provider;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.ciblock.commons.Worksheet;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 30.05.15
 * Time: 17:21
 */
public abstract class AbstractWorksheetProvider implements WorksheetProvider {
    @NotNull
    private final Map<String, String> worksheetIds;

    @Inject
    protected AbstractWorksheetProvider(@NotNull final Properties properties) {
        final String worksheetKey = "ciblock.spreadsheet." + getName();
        worksheetIds = properties.stringPropertyNames().stream()
                .filter(key -> key.startsWith(worksheetKey))
                .collect(Collectors.toMap(
                        key -> key.substring(worksheetKey.length() + 1),
                        properties::getProperty
                ));
    }

    @NotNull
    @Override
    public Worksheet upload(@NotNull final String worksheetId) throws RecentUpdateException {
        try {
            return uploadImpl(worksheetIds.get(worksheetId));
        } catch (RecentUpdateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public abstract Worksheet uploadImpl(@NotNull final String worksheetId) throws Exception;

    @NotNull
    public abstract String getName();

    @Nullable
    protected static String commented(@Nullable final String value) {
        return value == null || value.isEmpty() || isCommented(value) ? null : value;
    }

    protected static boolean isCommented(@NotNull final String value) {
        return value.startsWith("//");
    }
}
