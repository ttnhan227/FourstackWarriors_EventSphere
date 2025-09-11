package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.services.ParticipantService;
import fpt.aptech.eventsphere.services.ParticipantServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/participant")
@PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Get participant statistics
            int totalRegistrations = participantService.getTotalRegistrations();
            int attendedEvents = participantService.getAttendedEventsCount();
            
            // Get upcoming and past events
            List<Events> upcomingEvents = participantService.getUpcomingEvents();
            List<Events> pastEvents = participantService.getPastEvents();
            
            // Add attributes to the model
            model.addAttribute("totalRegistrations", totalRegistrations);
            model.addAttribute("attendedEvents", attendedEvents);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("pastEvents", pastEvents);
            
            return "participant/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error/error";
        }
    }
}
