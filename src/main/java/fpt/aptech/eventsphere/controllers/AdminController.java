package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.repositories.admin.*;
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
    private final EventRepository eventRepository;

    @Autowired
    private final FeedbackRepository feedbackRepository;

    public AdminController(UserRepository userRepository, EventRepository eventRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/index")
    public String adminDashboard(Model model) {
        //dashboard
        model.addAttribute("title", "Admin Dashboard");
        model.addAttribute("totalUsser", userRepository.countByIsActiveTrueAndIsDeletedFalse());
        model.addAttribute("totalEvents", eventRepository.countUpcomingEvents());
        model.addAttribute("complateEvents",eventRepository.countCompletedEvents());
        model.addAttribute("averageRating", feedbackRepository.getAverageRating());

        // chart
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        model.addAttribute("userRegistrationStats", userRepository.getUserRegistrationStats(thirtyDaysAgo));
        model.addAttribute("eventCreationStats", eventRepository.getEventCreationStats(thirtyDaysAgo));
        model.addAttribute("usersByDepartment", userRepository.countUsersByDepartment());
        model.addAttribute("eventsByDepartment", eventRepository.countEventsByDepartment());

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
        model.addAttribute("upcomingEvents", eventRepository.findUpcomingEvents(LocalDateTime.now()));
        model.addAttribute("pastEvents", eventRepository.findPastEvents(LocalDateTime.now()));
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
