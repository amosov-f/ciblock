package ru.spbu.astro.ciblock.depot;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Spreadsheet;
import ru.spbu.astro.ciblock.commons.Worksheet;
import ru.spbu.astro.ciblock.provider.WorksheetProvider;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
@ThreadSafe
public final class SpreadsheetDepot implements Supplier<Spreadsheet> {
    private static final Logger LOG = Logger.getLogger(SpreadsheetDepot.class.getName());

    public static final String DATA = "data";
    public static final String INFO = "info";

    private volatile Spreadsheet spreadsheet;

    private final ExecutorService executor = Executors.newScheduledThreadPool(2);

    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    public SpreadsheetDepot(@NotNull final Properties properties, @NotNull final WorksheetProvider provider) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            final Map<String, Future<Worksheet>> futureWorksheets = Stream.of(DATA, INFO).collect(Collectors.toMap(
                    (Function<String, String>) worksheetId -> worksheetId,
                    worksheetId -> executor.submit(() -> provider.upload(worksheetId))
            ));
            try {
                @SuppressWarnings("LocalVariableHidesMemberVariable")
                final Spreadsheet spreadsheet = new Spreadsheet();
                for (final String worksheetId : futureWorksheets.keySet()) {
                    spreadsheet.add(worksheetId, futureWorksheets.get(worksheetId).get());
                }
                this.spreadsheet = spreadsheet;
                latch.countDown();
            } catch (ExecutionException e) {
                if (e.getCause() != null && e.getCause() instanceof WorksheetProvider.RecentUpdateException) {
                    LOG.warning(e.getLocalizedMessage());
                } else {
                    LOG.log(Level.SEVERE, "Upload errror!", e);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                LOG.warning("Uploader interrupted!");
            }
        }, 0, Long.parseLong(properties.getProperty("ciblock.spreadsheet.retry_delay")), TimeUnit.SECONDS);
    }

    @NotNull
    @Override
    public Spreadsheet get() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return spreadsheet;
    }
}
