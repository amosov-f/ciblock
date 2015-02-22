package ru.spbu.astro.ciblock.servlet;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.ciblock.depot.ClassifierDepot;
import ru.spbu.astro.ciblock.depot.NearestNeighbourDepot;
import ru.spbu.astro.ciblock.commons.Spreadsheet;

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
    private final ClassifierDepot classifierDepot;
    @NotNull
    private final NearestNeighbourDepot nearestNeighbourDepot;

    @Inject
    public SubmitHttpServlet(@NotNull final ClassifierDepot classifierDepot, 
                             @NotNull final NearestNeighbourDepot nearestNeighbourDepot) 
    {
        this.classifierDepot = classifierDepot;
        this.nearestNeighbourDepot = nearestNeighbourDepot;
    }

    @Override
    protected void doGet(@NotNull final HttpServletRequest req, 
                         @NotNull final HttpServletResponse resp) throws ServletException, IOException 
    {
        final Map<String, Double> features = new HashMap<>();
        for (final String name : Collections.list(req.getParameterNames())) {
            features.put(name, new Double(req.getParameter(name)));
        }

        final Spreadsheet spreadsheet = classifierDepot.get();

        req.setAttribute("features", spreadsheet.getFeatures());
        req.setAttribute("answers", spreadsheet.getAnswers());

        req.setAttribute("feature values", features);
        req.setAttribute("values", classifierDepot.classify(features));

        req.setAttribute("nearest neighbours", nearestNeighbourDepot.getKNearestNeighbours(features, K));

        req.getRequestDispatcher("pages/report.jsp").forward(req, resp);
    }
}
