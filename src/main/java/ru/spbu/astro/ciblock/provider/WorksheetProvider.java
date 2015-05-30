package ru.spbu.astro.ciblock.provider;

import com.google.gdata.data.DateTime;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Worksheet;

/**
 * User: amosov-f
 * Date: 30.05.15
 * Time: 16:20
 */
public interface WorksheetProvider {
    @NotNull
    Worksheet upload(@NotNull String worksheetId) throws RecentUpdateException;

    class RecentUpdateException extends Exception {
        public RecentUpdateException(@NotNull final DateTime t) {
            super(DateTime.now() + ": time of last update " + t + " is very recent");
        }
    }
}
