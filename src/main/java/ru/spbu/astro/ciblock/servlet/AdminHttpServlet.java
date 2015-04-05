package ru.spbu.astro.ciblock.servlet;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.commons.Worksheet;
import ru.spbu.astro.ciblock.depot.SpreadsheetDepot;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: amosov-f
 * Date: 21.02.15
 * Time: 20:06
 */
@WebServlet("/admin")
public final class AdminHttpServlet extends HttpServlet {
    @NotNull
    private final SpreadsheetDepot spreadsheetDepot;

    @Inject
    public AdminHttpServlet(@NotNull final SpreadsheetDepot spreadsheetDepot) {
        this.spreadsheetDepot = spreadsheetDepot;
    }

    @Override
    protected void doGet(@NotNull final HttpServletRequest req, 
                         @NotNull final HttpServletResponse resp) throws ServletException, IOException 
    {
        final Worksheet worksheet = spreadsheetDepot.get().get(SpreadsheetDepot.DATA);

        req.setAttribute("squares", worksheet.getDoubles("площадь"));
        req.setAttribute("altitudes", worksheet.getDoubles("высота"));
        req.setAttribute("perimeters", worksheet.getDoubles("периметр"));

        req.getRequestDispatcher("pages/admin.jsp").forward(req, resp);
    }
}
