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
import ru.spbu.astro.ciblock.commons.Factor;
import ru.spbu.astro.ciblock.commons.Spreadsheet;
import ru.spbu.astro.ciblock.commons.Vector;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


@ThreadSafe
public class SpreadsheetDepot implements Supplier<Spreadsheet> {
    private static final Logger LOGGER = Logger.getLogger(SpreadsheetDepot.class.getName());
    
    private static final String KEY = "1LSpPXxsrMTiFDyBz08OTYh0xRhyou-21f-k1xfGPHPs";

    private volatile Spreadsheet spreadsheet;
    
    @NotNull
    private final String worksheetId;
    @NotNull
    private final String username;
    @NotNull
    private final String password;
    private final long recentTime;

    @NotNull
    private final CountDownLatch latch = new CountDownLatch(1);

    public SpreadsheetDepot(@NotNull final String worksheetId, @NotNull final Properties properties) {
        this.worksheetId = worksheetId;
        username = properties.getProperty("username");
        password = properties.getProperty("password");
        recentTime = Long.parseLong(properties.getProperty("ciblock.spreadsheet.recent"));
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    upload();
                } catch (RecentUpdateException e) {
                    LOGGER.warning(e.getLocalizedMessage());
                } catch (ServiceException | IOException | RuntimeException e) {
                    LOGGER.log(Level.SEVERE, "Upload errror!", e);
                }
            }
        }, 0, Long.parseLong(properties.getProperty("ciblock.spreadsheet.retry_delay")), TimeUnit.SECONDS);
    }

    @NotNull
    @Override
    public final Spreadsheet get() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return spreadsheet;
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    private void upload() throws ServiceException, IOException, RecentUpdateException {
        final Spreadsheet spreadsheet = new Spreadsheet();

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
            final String description = cell.getCell().getValue();

            final Factor factor = Factor.parse(description);
            
            if (factor != null) {
                spreadsheet.addFactor(factor);
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
                    x.add(names.get(i), row.getCustomElements().getValue(tags.get(i)));
                }
            }

            spreadsheet.add(x);

            for (final Factor.Class clazz : classes) {
                final String value = x.get(clazz.getName());
                if (value != null) {
                    clazz.addValue(value);
                }
            }
        }

        this.spreadsheet = spreadsheet;
        latch.countDown();
    }

    @Singleton
    @ThreadSafe
    public static class DataDepot extends SpreadsheetDepot {
        @Inject
        public DataDepot(@NotNull final Properties properties) {
            super("od6", properties);
        }
    }

    @Singleton
    @ThreadSafe
    public static class InfoDepot extends SpreadsheetDepot {
        @Inject
        public InfoDepot(@NotNull final Properties properties) {
            super("od4", properties);
        }
    }

    public static class RecentUpdateException extends Exception {
        public RecentUpdateException(final DateTime t) {
            super(DateTime.now() + ": time of last update " + t + " is very recent");
        }
    }
}
