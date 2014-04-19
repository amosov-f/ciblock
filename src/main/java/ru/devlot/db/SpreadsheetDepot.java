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


public class SpreadsheetDepot {

    private Spreadsheet spreadsheet;

    private static final long SLEEP_TIME = 60000;
    private static final long RECENT_TIME = 60000;


    public SpreadsheetDepot() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    this.spreadsheet = uploadSpreadsheet();
                } catch (RecentUpdateException e) {
                    e.printStackTrace(System.out);
                } catch (ServiceException | IOException e) {
                    e.printStackTrace();
                }

                System.out.println(this.spreadsheet);

                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public Spreadsheet uploadSpreadsheet() throws ServiceException, IOException, RecentUpdateException {
        SpreadsheetService service = new SpreadsheetService("devlot-1.0.0");
        service.setUserCredentials(LotDeveloperEngine.username, LotDeveloperEngine.password);
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        Spreadsheet spreadsheet = new Spreadsheet();
        URL cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/tC-ZyoR2ayny8PP0JKBUDLw/od6/private/full?max-row=1&min-col=2");
        CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

        if (DateTime.now().getValue() - cellFeed.getUpdated().getValue() < RECENT_TIME) {
            throw new RecentUpdateException(cellFeed.getUpdated());
        }

        for (CellEntry cell : cellFeed.getEntries()) {
            Factor factor = new Factor(cell.getCell().getValue());
            spreadsheet.addFactor(factor);
        }

        URL listFeedUrl = new URL("https://spreadsheets.google.com/feeds/list/tC-ZyoR2ayny8PP0JKBUDLw/od6/private/full");
        ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
        for (ListEntry row : listFeed.getEntries()) {
            Vector vector = new Vector(row.getTitle().getPlainText());

            List<String> tags = new ArrayList<>(row.getCustomElements().getTags());
            tags = tags.subList(1, tags.size());
            for (String tag : tags) {
                vector.add(new Double(row.getCustomElements().getValue(tag)));
            }

            spreadsheet.add(vector);
        }

        return spreadsheet;
    }

    public class RecentUpdateException extends Exception {
        public RecentUpdateException(DateTime t) {
            super(DateTime.now() + ": time of last update " + t + " is very recent");
        }
    }
}