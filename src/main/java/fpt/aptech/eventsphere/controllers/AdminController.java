package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.repositories.admin.*;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AdminEventRepository adminEventRepository;

    @Autowired
    private final AdminFeedbackRepository adminFeedbackRepository;

    public AdminController(UserRepository userRepository, AdminEventRepository adminEventRepository, AdminFeedbackRepository adminFeedbackRepository) {
        this.userRepository = userRepository;
        this.adminEventRepository = adminEventRepository;
        this.adminFeedbackRepository = adminFeedbackRepository;
    }

    @GetMapping("/index")
    public String adminDashboard(Model model) {
        //dashboard
        model.addAttribute("title", "Admin Dashboard");
        model.addAttribute("totalUsser", userRepository.countByIsActiveTrueAndIsDeletedFalse());
        model.addAttribute("totalEvents", adminEventRepository.countUpcomingEvents());
        model.addAttribute("complateEvents", adminEventRepository.countCompletedEvents());
        model.addAttribute("averageRating", adminFeedbackRepository.getAverageRating());

        // chart
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        model.addAttribute("userRegistrationStats", userRepository.getUserRegistrationStats(thirtyDaysAgo));
        model.addAttribute("eventCreationStats", adminEventRepository.getEventCreationStats(thirtyDaysAgo));
        model.addAttribute("usersByDepartment", userRepository.countUsersByDepartment());
        model.addAttribute("eventsByDepartment", adminEventRepository.countEventsByDepartment());

        return "admin/index";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("title", "User Managerment");
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("inactiveUsers", userRepository.findByIsActiveFalse());
        return "admin/users";
    }


    @GetMapping("/events")
    public String eventManagement(Model model) {
        model.addAttribute("title", "Event Management");
        model.addAttribute("upcomingEvents", adminEventRepository.findUpcomingEvents(LocalDateTime.now()));
        model.addAttribute("pastEvents", adminEventRepository.findPastEvents(LocalDateTime.now()));
        return "admin/events";
    }

    @PostMapping("/users/{id}/toggle-status")
    @ResponseBody
    public String toggleUserStatus(@PathVariable int id) {
        Users user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setActive(!user.isActive());
            userRepository.save(user);
            return "success";
        }
        return "error";
    }
}
