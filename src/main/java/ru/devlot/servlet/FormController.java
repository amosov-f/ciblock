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

import java.util.HashMap;
import java.util.Map;

import static ru.devlot.model.Factor.Answer;
import static ru.devlot.model.Factor.Feature;

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

        model.addAttribute("features", spreadsheet.getFactors(Feature.class));

        Vector example = spreadsheet.iterator().next();
        model.addAttribute("example", example);

        return "form";
    }

    @RequestMapping("/submit")
    public String submit(@RequestParam String feature_json, Model model) throws Exception {
        Map<String, Double> features = new HashMap<>();
        for (Map.Entry<String, String> entry : ((Map<String, String>) JSON.parse(feature_json)).entrySet()) {
            features.put(entry.getKey(), new Double(entry.getValue()));
        }

        Spreadsheet spreadsheet = classifierDepot.get();


        model.addAttribute("features", spreadsheet.getFactors(Feature.class));
        model.addAttribute("answers", spreadsheet.getFactors(Answer.class));

        Map<String, Double> values = new HashMap<>(features);
        values.putAll(classifierDepot.classify(features));
        model.addAttribute("values", values);

        model.addAttribute("nearestNeighbours", nearestNeighbourDepot.getKNearestNeighbours(features, K));

        return "report";
    }

    @RequestMapping("/how")
    public String how() {
        return "how";
    }

}
