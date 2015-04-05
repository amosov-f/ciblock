package ru.spbu.astro.ciblock.servlet;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.depot.ModelDepot;
import ru.spbu.astro.ciblock.commons.Worksheet;
import ru.spbu.astro.ciblock.commons.Vector;
import ru.spbu.astro.ciblock.depot.SpreadsheetDepot;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: amosov-f
 * Date: 22.02.15
 * Time: 1:24
 */
@WebServlet("/form")
public final class FormHttpServlet extends HttpServlet {
    @NotNull
    private final ModelDepot modelDepot;

    @Inject
    public FormHttpServlet(@NotNull final ModelDepot modelDepot) {
        this.modelDepot = modelDepot;
    }

    @Override
    protected void doGet(@NotNull final HttpServletRequest req, 
                         @NotNull final HttpServletResponse resp) throws ServletException, IOException 
    {
        final Worksheet worksheet = modelDepot.getSpreadsheet().get(SpreadsheetDepot.DATA);
        req.setAttribute("features", worksheet.getFeatures());
        final Vector example = worksheet.getVectors().get(0);
        req.setAttribute("example", example);
        req.getRequestDispatcher("pages/form.jsp").forward(req, resp);
    }
}
