package ru.spbu.astro.ciblock.depot;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.ciblock.commons.Factor;
import ru.spbu.astro.ciblock.commons.Spreadsheet;
import ru.spbu.astro.ciblock.commons.Vector;
import ru.spbu.astro.ciblock.commons.Worksheet;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


@Singleton
@ThreadSafe
public final class SpreadsheetDepot implements Supplier<Spreadsheet> {
    private static final Logger LOG = Logger.getLogger(SpreadsheetDepot.class.getName());

    public static final String DATA = "od6";
    public static final String INFO = "od4";

    private static final String KEY = "1LSpPXxsrMTiFDyBz08OTYh0xRhyou-21f-k1xfGPHPs";
    private static final String[] WORKSHEET_IDS = {DATA, INFO};

    private volatile Spreadsheet spreadsheet;

    @NotNull
    private final String username;
    @NotNull
    private final String password;
    private final long recentTime;

    private final ExecutorService executor = Executors.newScheduledThreadPool(2);

    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    public SpreadsheetDepot(@NotNull final Properties properties) {
        username = properties.getProperty("username");
        password = properties.getProperty("password");
        recentTime = Long.parseLong(properties.getProperty("ciblock.spreadsheet.recent"));
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            final Map<String, Future<Worksheet>> futureWorksheets = new HashMap<>();
            for (final String worksheetId : WORKSHEET_IDS) {
                futureWorksheets.put(worksheetId, executor.submit(new Callable<Worksheet>() {
                    @Override
                    public Worksheet call() throws Exception {
                        return upload(worksheetId);
                    }
                }));
            }
            try {
                @SuppressWarnings("LocalVariableHidesMemberVariable")
                final Spreadsheet spreadsheet = new Spreadsheet();
                for (final String worksheetId : futureWorksheets.keySet()) {
                    spreadsheet.add(worksheetId, futureWorksheets.get(worksheetId).get());
                }
                this.spreadsheet = spreadsheet;
                latch.countDown();
            } catch (ExecutionException e) {
                if (e.getCause() != null && e.getCause() instanceof RecentUpdateException) {
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

    @NotNull
    private Worksheet upload(@NotNull final String worksheetId) throws ServiceException, IOException, RecentUpdateException {
        final Worksheet worksheet = new Worksheet();

        final SpreadsheetService service = new SpreadsheetService("ciblock-1.0.0");
        service.setUserCredentials(username, password);
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        final URL cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/" + KEY + "/" + worksheetId + "/private/full?max-row=1&min-col=2");
        final CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

        if (DateTime.now().getValue() - cellFeed.getUpdated().getValue() < recentTime) {
            throw new RecentUpdateException(cellFeed.getUpdated());
        }

        // table reading
        final List<Factor.Class> classes = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        for (final CellEntry cell : cellFeed.getEntries()) {
            final String description = commented(cell.getCell().getValue());

            final Factor factor = description != null ? Factor.parse(description) : null;
            
            if (factor != null) {
                worksheet.addFactor(factor);
                if (factor instanceof Factor.Class) {
                    classes.add((Factor.Class) factor);
                }
                names.add(factor.getName());
            } else {
                names.add(null);
            }
        }

        final URL listFeedUrl = new URL("https://spreadsheets.google.com/feeds/list/" + KEY + "/" + worksheetId + "/private/full");

        final ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
        for (final ListEntry row : listFeed.getEntries()) {
            final Vector x = new Vector(row.getTitle().getPlainText());

            List<String> tags = new ArrayList<>(row.getCustomElements().getTags());
            tags = tags.subList(1, tags.size());
            for (int i = 0; i < tags.size(); ++i) {
                if (names.get(i) != null) {
                    x.add(names.get(i), commented(row.getCustomElements().getValue(tags.get(i))));
                }
            }

            worksheet.add(x);

            for (final Factor.Class clazz : classes) {
                final String value = x.get(clazz.getName());
                if (value != null) {
                    clazz.addValue(value);
                }
            }
        }
        return worksheet;
    }

    @Nullable
    private static String commented(@Nullable final String value) {
        return value == null || value.startsWith("//") ? null : value;
    }

    public static class RecentUpdateException extends Exception {
        public RecentUpdateException(final DateTime t) {
            super(DateTime.now() + ": time of last update " + t + " is very recent");
        }
    }
}
