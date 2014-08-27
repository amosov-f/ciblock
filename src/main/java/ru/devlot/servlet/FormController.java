package ru.devlot.servlet;

import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.devlot.db.ClassifierDepot;
import ru.devlot.db.NearestNeighbourDepot;
import ru.devlot.model.Spreadsheet;
import ru.devlot.model.Vector;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FormController {

    private static final int K = 3;

    @Autowired
    ClassifierDepot classifierDepot;

    @Autowired
    NearestNeighbourDepot nearestNeighbourDepot;

    @RequestMapping("/")
    public String form(Model model) {
        Spreadsheet spreadsheet = classifierDepot.get();

        model.addAttribute("features", spreadsheet.getFeatures());

        Vector example = spreadsheet.iterator().next();
        model.addAttribute("example", example);

        return "form";
    }

    @RequestMapping("/submit")
    public String submit(HttpServletRequest request, Model model) throws Exception {
        Map<String, Double> features = new HashMap<>();
        for (String name : Collections.list(request.getParameterNames())) {
            features.put(name, new Double(request.getParameter(name)));
        }

        Spreadsheet spreadsheet = classifierDepot.get();

        model.addAttribute("features", spreadsheet.getFeatures());
        model.addAttribute("answers", spreadsheet.getAnswers());

        model.addAttribute("feature values", features);
        model.addAttribute("values", classifierDepot.classify(features));

        model.addAttribute("nearest neighbours", nearestNeighbourDepot.getKNearestNeighbours(features, K));

        return "report";
    }

    @RequestMapping("/how")
    public String how() {
        return "how";
    }

}
