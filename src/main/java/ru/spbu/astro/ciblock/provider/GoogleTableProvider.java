package ru.spbu.astro.ciblock.provider;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Factor;
import ru.spbu.astro.ciblock.commons.Vector;
import ru.spbu.astro.ciblock.commons.Worksheet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * User: amosov-f
 * Date: 30.05.15
 * Time: 16:26
 */
public final class GoogleTableProvider extends AbstractWorksheetProvider {
    private static final String KEY = "1LSpPXxsrMTiFDyBz08OTYh0xRhyou-21f-k1xfGPHPs";

    @NotNull
    private final String username;
    @NotNull
    private final String password;
    private final long recentTime;

    @Inject
    public GoogleTableProvider(@NotNull final Properties properties) {
        super(properties);
        username = properties.getProperty("username");
        password = properties.getProperty("password");
        recentTime = Long.parseLong(properties.getProperty("ciblock.spreadsheet.recent"));

    }

    @NotNull
    public Worksheet uploadImpl(@NotNull final String worksheetId) throws RecentUpdateException, ServiceException, IOException {
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

        for (final ListEntry row : service.getFeed(listFeedUrl, ListFeed.class).getEntries()) {
            final Vector x = new Vector(row.getTitle().getPlainText());

            List<String> tags = new ArrayList<>(row.getCustomElements().getTags());
            tags = tags.subList(1, tags.size());
            for (int i = 0; i < tags.size(); ++i) {
                if (names.get(i) != null) {
                    x.add(names.get(i), commented(row.getCustomElements().getValue(tags.get(i))));
                }
            }

            worksheet.add(x);

            classes.forEach(clazz -> Optional.ofNullable(x.get(clazz.getName())).ifPresent(clazz::addValue));
        }
        return worksheet;
    }

    @NotNull
    @Override
    public String getName() {
        return "google";
    }
}
