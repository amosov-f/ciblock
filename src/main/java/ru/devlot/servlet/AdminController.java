package ru.devlot.servlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.devlot.db.ClassifierDepot;
import ru.devlot.model.Spreadsheet;

@Controller
public class AdminController {

    @Autowired
    ClassifierDepot classifierDepot;

    @RequestMapping("/plot")
    public String plot(Model model) {
        Spreadsheet spreadsheet = classifierDepot.get();

        model.addAttribute("squares", spreadsheet.getDoubles("площадь"));
        model.addAttribute("altitudes", spreadsheet.getDoubles("высота"));
        model.addAttribute("perimeters", spreadsheet.getDoubles("периметр"));

        return "chart";
    }

}
