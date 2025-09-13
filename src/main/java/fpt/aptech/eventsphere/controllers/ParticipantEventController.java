package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.services.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/participant")
@PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
public class ParticipantEventController {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantEventController.class);
    private static final int PAGE_SIZE = 10;
    private final ParticipantService participantService;
    private final EventRepository eventRepository;

    public ParticipantEventController(ParticipantService participantService, EventRepository eventRepository) {
        this.participantService = participantService;
        this.eventRepository = eventRepository;
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

    @GetMapping("/events/list")
    public String listEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "category", required = false) String category,
            Model model) {
        logger.info("Fetching events with category: {}", category);
        try {
            List<Events> events;
            if (category != null && !category.isEmpty()) {
                // If category is provided, filter by category
                logger.debug("Fetching events for category: {}", category);
                events = participantService.getUpcomingEventsByCategory(category);
                logger.debug("Found {} events for category: {}", events.size(), category);
            } else {
                // Otherwise, get all upcoming events
                logger.debug("Fetching all upcoming events");
                events = participantService.getAllUpcomingEvents();
                logger.debug("Found {} upcoming events", events.size());
            }

            // Add pagination
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), events.size());
            Page<Events> eventPage = new org.springframework.data.domain.PageImpl<>(
                    events.subList(start, end),
                    pageable,
                    events.size()
            );

            // Get distinct categories for filter
            List<String> categories = events.stream()
                    .map(Events::getCategory)
                    .filter(cat -> cat != null && !cat.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            model.addAttribute("events", eventPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", eventPage.getTotalPages());
            model.addAttribute("category", category);
            model.addAttribute("categories", categories);
            model.addAttribute("isPast", false);

            return "participant/events/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error loading events: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable("id") int id, Model model) {
        try {
            logger.info("Attempting to find event with ID: {}", id);
            // Use a custom query to fetch the event with its organizer and venue
            Events event = eventRepository.findByIdWithOrganizerAndVenue(id);
            
            if (event != null) {
                logger.info("Found event: {}", event.getTitle());
                
                // Check if current user is registered for this event and get registration details
                boolean isRegistered = participantService.isUserRegisteredForEvent(id);
                int availableSeats = participantService.getAvailableSeats(id);
                
                // Initialize default values
                String registrationStatus = "";
                LocalDateTime registrationDate = null;
                Registrations registration = null;
                
                // Get registration status and date if user is registered
                if (isRegistered) {
                    registration = participantService.getRegistrationForEvent(id);
                    if (registration != null) {
                        registrationStatus = registration.getStatus().name();
                        registrationDate = registration.getRegisteredOn();
                    }
                }
                
                model.addAttribute("event", event);
                model.addAttribute("isRegistered", isRegistered);
                model.addAttribute("availableSeats", availableSeats);
                model.addAttribute("registrationStatus", registrationStatus);
                model.addAttribute("registrationDate", registrationDate);
                model.addAttribute("registration", registration);
                
                return "participant/events/view";
            } else {
                logger.warn("Event not found with ID: {}", id);
                model.addAttribute("error", "Event not found");
                return "error/404";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading event: " + e.getMessage());
            return "error/error";
        }
    }
    
    @PostMapping("/events/{eventId}/register")
    public String registerForEvent(@PathVariable("eventId") int eventId, RedirectAttributes redirectAttributes) {
        try {
            // Register with PENDING status
            participantService.registerForEvent(eventId);
            redirectAttributes.addFlashAttribute("success", "Successfully registered for the event! Your registration is pending confirmation.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/participant/events/" + eventId;
    }
    
    @PostMapping("/events/{eventId}/confirm")
    public String confirmRegistration(@PathVariable("eventId") int eventId, RedirectAttributes redirectAttributes) {
        try {
            participantService.confirmRegistration(eventId);
            redirectAttributes.addFlashAttribute("success", "Your registration has been confirmed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/participant/events/" + eventId;
    }
    
    @PostMapping("/events/{eventId}/cancel")
    public String cancelRegistration(@PathVariable("eventId") int eventId, RedirectAttributes redirectAttributes) {
        try {
            participantService.cancelRegistration(eventId);
            redirectAttributes.addFlashAttribute("success", "Successfully cancelled your registration!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/participant/events/" + eventId;
    }

    @GetMapping("/events/past")
    public String pastEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            List<Events> pastEvents = participantService.getAllPastEvents();

            // Add pagination
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), pastEvents.size());
            Page<Events> eventPage = new org.springframework.data.domain.PageImpl<>(
                    pastEvents.subList(start, end),
                    pageable,
                    pastEvents.size()
            );

            // Get distinct categories for filter
            List<String> categories = pastEvents.stream()
                    .map(Events::getCategory)
                    .filter(cat -> cat != null && !cat.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            model.addAttribute("events", eventPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", eventPage.getTotalPages());
            model.addAttribute("categories", categories);
            model.addAttribute("isPast", true);

            return "participant/events/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error loading past events: " + e.getMessage());
            return "error/error";
        }
    }
}
