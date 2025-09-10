package fpt.aptech.eventsphere.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    
    @GetMapping("")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin/index";
                } else if (authority.getAuthority().equals("ROLE_ORGANIZER")) {
                    return "redirect:/organizer/index";
                } else if (authority.getAuthority().equals("ROLE_PARTICIPANT")) {
                    return "redirect:/participant/dashboard";
                }
            }
        }
        return "redirect:/auth/login";
    }
    
    @GetMapping("/generic")
    public String generic() {
        return "home/generic";
    }
    
    @GetMapping("/elements")
    public String elements() {
        return "home/elements";
    }
}
