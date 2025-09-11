package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.services.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/participant")
@PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
public class ParticipantEventController {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantEventController.class);
    private static final int PAGE_SIZE = 10;
    private final ParticipantService participantService;

    public ParticipantEventController(ParticipantService participantService) {
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

    @GetMapping("/events/view/{id}")
    public String viewEvent(@PathVariable("id") Long id, Model model) {
        try {
            // In a real implementation, you would have a method in ParticipantService to get event by ID
            // For now, we'll find it from the upcoming events
            Optional<Events> event = participantService.getUpcomingEvents().stream()
                    .filter(e -> e.getEventId() == id)
                    .findFirst();

            if (event.isPresent()) {
                model.addAttribute("event", event.get());
                return "participant/events/view";
            } else {
                // Check past events if not found in upcoming
                event = participantService.getPastEvents().stream()
                        .filter(e -> e.getEventId() == id)
                        .findFirst();

                if (event.isPresent()) {
                    model.addAttribute("event", event.get());
                    return "participant/events/view";
                } else {
                    model.addAttribute("error", "Event not found");
                    return "error/404";
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading event: " + e.getMessage());
            return "error/error";
        }
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
