package fpt.aptech.eventsphere.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    
    @GetMapping("")
    public String home() {
        return "home/index";
    }
    
    @GetMapping("/generic")
    public String generic() {
        return "home/generic";
    }
    
    @GetMapping("/elements")
    public String elements() {
        return "home/elements";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/admin/index")
    public String admin() {
        return "index";
    }
}
