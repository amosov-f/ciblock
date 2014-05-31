package ru.devlot.db;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;
import ru.devlot.LotDeveloperEngine;
import ru.devlot.model.Factor;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ru.devlot.LotDeveloperEngine.SERVER_NAME;
import static ru.devlot.LotDeveloperEngine.SLEEP_TIME;

public class SpreadsheetDepot {

    private Spreadsheet spreadsheet;

    private static final String KEY = "tC-ZyoR2ayny8PP0JKBUDLw";

    private final String worksheetId;

    private static final long RECENT_TIME;
    static {
        if (System.getProperty("user.name").equals(SERVER_NAME)) {
            RECENT_TIME = 60 * 60 * 1000;
        } else {
            RECENT_TIME = 10 * 1000;
        }
    }

    public SpreadsheetDepot(String worksheetId) {
        this.worksheetId = worksheetId;
        new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (SpreadsheetDepot.this) {
                    try {
                        spreadsheet = upload();
                    } catch (RecentUpdateException e) {
                        e.printStackTrace(System.out);
                    } catch (ServiceException | IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println(spreadsheet);
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized Spreadsheet get() {
        return spreadsheet;
    }

    private Spreadsheet upload() throws ServiceException, IOException, RecentUpdateException {
        Spreadsheet spreadsheet = new Spreadsheet();

        SpreadsheetService service = new SpreadsheetService("devlot-1.0.0");
        service.setUserCredentials(LotDeveloperEngine.username, LotDeveloperEngine.password);
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        URL cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/" + KEY + "/" + worksheetId + "/private/full?max-row=1&min-col=2");
        CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

        if (DateTime.now().getValue() - cellFeed.getUpdated().getValue() < RECENT_TIME) {
            throw new RecentUpdateException(cellFeed.getUpdated());
        }

        List<String> names = new ArrayList<>();
        for (CellEntry cell : cellFeed.getEntries()) {
            String description = cell.getCell().getValue();

            Factor factor = Factor.parse(description);
            spreadsheet.addFactor(factor);
            names.add(factor.getName());
        }

        URL listFeedUrl = new URL("https://spreadsheets.google.com/feeds/list/" + KEY + "/" + worksheetId + "/private/full");

        ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
        for (ListEntry row : listFeed.getEntries()) {
            Vector vector = new Vector(row.getTitle().getPlainText());

            List<String> tags = new ArrayList<>(row.getCustomElements().getTags());
            tags = tags.subList(1, tags.size());
            for (int i = 0; i < tags.size(); ++i) {
                vector.add(names.get(i), row.getCustomElements().getValue(tags.get(i)));
            }

            spreadsheet.add(vector);
        }

        for (Vector x : spreadsheet) {
            for (Factor.Class type : spreadsheet.getFactors(Factor.Class.class)) {
                type.add(x.get(type.getName()));
            }
        }

        return spreadsheet;
    }

    public static class DataDepot extends SpreadsheetDepot {

        public DataDepot() {
            super("od6");
        }

    }

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
