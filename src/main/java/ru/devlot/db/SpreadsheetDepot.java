package ru.devlot.db;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;
import org.jetbrains.annotations.NotNull;
import ru.devlot.CiBlockServer;
import ru.devlot.model.Factor;
import ru.devlot.model.Spreadsheet;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ru.devlot.CiBlockServer.SERVER_NAME;
import static ru.devlot.CiBlockServer.SLEEP_TIME;
import static ru.devlot.model.Factor.Class.ExpandingClass;
import static ru.devlot.model.Vector.ExpandingVector;

@ThreadSafe
public class SpreadsheetDepot {
    @NotNull
    private Spreadsheet spreadsheet;

    private static final String KEY = "tC-ZyoR2ayny8PP0JKBUDLw";

    @NotNull
    private final String worksheetId;

    private static final long RECENT_TIME;
    static {
        if (System.getProperty("user.name").equals(SERVER_NAME)) {
            RECENT_TIME = 60 * 60 * 1000;
        } else {
            RECENT_TIME = 10 * 1000;
        }
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    public SpreadsheetDepot(@NotNull final String worksheetId) {
        this.worksheetId = worksheetId;
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    upload();
                } catch (RecentUpdateException e) {
                    e.printStackTrace(System.out);
                } catch (ServiceException | IOException e) {
                    e.printStackTrace();
                }

                System.out.println(spreadsheet);

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @NotNull
    public Spreadsheet get() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spreadsheet;
    }

    private void upload() throws ServiceException, IOException, RecentUpdateException {
        Spreadsheet.ExpandingSpreadsheet spreadsheet = new Spreadsheet.ExpandingSpreadsheet();

        SpreadsheetService service = new SpreadsheetService("ciblock-1.0.0");
        service.setUserCredentials(CiBlockServer.username, CiBlockServer.password);
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        URL cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/" + KEY + "/" + worksheetId + "/private/full?max-row=1&min-col=2");
        CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

        if (DateTime.now().getValue() - cellFeed.getUpdated().getValue() < RECENT_TIME) {
            throw new RecentUpdateException(cellFeed.getUpdated());
        }

        List<ExpandingClass> classes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (CellEntry cell : cellFeed.getEntries()) {
            String description = cell.getCell().getValue();

            Factor factor = Factor.parse(description);

            spreadsheet.addFactor(factor);

            if (factor instanceof ExpandingClass) {
                classes.add((ExpandingClass) factor);
            }
            names.add(factor.getName());
        }

        URL listFeedUrl = new URL("https://spreadsheets.google.com/feeds/list/" + KEY + "/" + worksheetId + "/private/full");

        ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
        for (ListEntry row : listFeed.getEntries()) {
            ExpandingVector x = new ExpandingVector(row.getTitle().getPlainText());

            List<String> tags = new ArrayList<>(row.getCustomElements().getTags());
            tags = tags.subList(1, tags.size());
            for (int i = 0; i < tags.size(); ++i) {
                x.add(names.get(i), row.getCustomElements().getValue(tags.get(i)));
            }

            spreadsheet.add(x);

            for (ExpandingClass clazz : classes) {
                if (x.get(clazz.getName()) != null) {
                    clazz.addValue(x.get(clazz.getName()));
                }
            }
        }

        this.spreadsheet = spreadsheet;
        latch.countDown();
    }

    @ThreadSafe
    public static class DataDepot extends SpreadsheetDepot {

        public DataDepot() {
            super("od6");
        }

    }

    @ThreadSafe
    public static class InfoDepot extends SpreadsheetDepot {

        public InfoDepot() {
            super("od4");
        }

    }

    public class RecentUpdateException extends Exception {

        public RecentUpdateException(DateTime t) {
            super(DateTime.now() + ": time of last update " + t + " is very recent");
        }

    }

}
