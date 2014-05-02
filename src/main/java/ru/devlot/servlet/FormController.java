package ru.devlot.servlet;

import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.devlot.db.ClassifierDepot;
import ru.devlot.model.Spreadsheet;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FormController {

    @Autowired
    ClassifierDepot classifierDepot;

    @RequestMapping("/")
    public String form(Model model) {
        Spreadsheet spreadsheet = classifierDepot.getSpreadsheet();

        model.addAttribute("features", spreadsheet.getFeatures());

        model.addAttribute("squares", spreadsheet.getDoubles("площадь"));
        model.addAttribute("altitudes", spreadsheet.getDoubles("высота"));
        model.addAttribute("perimeters", spreadsheet.getDoubles("периметр"));

        return "form";
    }

    @RequestMapping("/submit")
    public String submit(@RequestParam String feature_json, Model model) throws Exception {
        Map<Integer, Double> features = new HashMap<>();
        for (Map.Entry<String, String> entry : ((Map<String, String>) JSON.parse(feature_json)).entrySet()) {
            features.put(new Integer(entry.getKey()), new Double(entry.getValue()));
        }

        Spreadsheet spreadsheet = classifierDepot.getSpreadsheet();

        model.addAttribute("features", spreadsheet.getFeatures());
        model.addAttribute("answers", spreadsheet.getAnswers());

        Map<Integer, Double> values = new HashMap<>(features);
        values.putAll(classifierDepot.classify(features));
        model.addAttribute("values", values);

        System.out.println(model);

        //System.out.println(spreadsheet.getDoubles("площадь"));
        //System.out.println(spreadsheet.getDoubles("высота"));
        //System.out.println(spreadsheet.getDoubles("периметр"));

        return "report";
    }

}
