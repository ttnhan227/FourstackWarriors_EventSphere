package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    UtilityService utilityService;

    @Autowired
    public HomeController(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("user_count", utilityService.countUsers());
        model.addAttribute("event_count", utilityService.countEvents());
        return "index";
    }
}
