package ru.spbu.astro.ciblock.servlet;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.depot.ModelDepot;
import ru.spbu.astro.ciblock.commons.Worksheet;
import ru.spbu.astro.ciblock.depot.SpreadsheetDepot;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 22.02.15
 * Time: 1:49
 */
@WebServlet("/submit")
public final class SubmitHttpServlet extends HttpServlet {
    private static final int K = 3;
    
    @NotNull
    private final ModelDepot modelDepot;

    @Inject
    public SubmitHttpServlet(@NotNull final ModelDepot modelDepot) {
        this.modelDepot = modelDepot;
    }

    @Override
    protected void doGet(@NotNull final HttpServletRequest req, 
                         @NotNull final HttpServletResponse resp) throws ServletException, IOException 
    {
        final Map<String, Double> features = new HashMap<>();
        for (final String name : Collections.list(req.getParameterNames())) {
            features.put(name, new Double(req.getParameter(name)));
        }

        final Worksheet worksheet = modelDepot.getSpreadsheet().get(SpreadsheetDepot.DATA);

        req.setAttribute("features", worksheet.getFeatures());
        req.setAttribute("answers", worksheet.getAnswers());

        req.setAttribute("feature values", features);
        req.setAttribute("values", modelDepot.classify(features));

        req.setAttribute("nearest neighbours", modelDepot.getKNearestNeighbours(features, K));

        req.getRequestDispatcher("pages/report.jsp").forward(req, resp);
    }
}
