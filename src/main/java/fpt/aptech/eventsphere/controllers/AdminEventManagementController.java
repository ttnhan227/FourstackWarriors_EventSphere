package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.admin.AdminEventDTO;
import fpt.aptech.eventsphere.dto.admin.AdminEventSearchDTO;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.services.Admin.AdminEventManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/event-management")
@RequiredArgsConstructor
@Slf4j
public class AdminEventManagementController {

    @Qualifier("adminEventManagementServiceImpl")
    private final AdminEventManagementService eventManagementService;

    @GetMapping
    public String eventManagement(
            @ModelAttribute AdminEventSearchDTO searchDTO,
            Model model) {

        log.info("Accessing event management page with search criteria");

        try {
            Page<AdminEventDTO> eventsPage = eventManagementService.searchAndFilterEvents(searchDTO);

            Long totalEvents = eventManagementService.getTotalEvents();
            Long ongoingEvents = eventManagementService.getOngoingEvents();
            Long cancelledEvents = eventManagementService.getCancelledEvents();

            Map<String, Long> statusStats = eventManagementService.getEventStatusStatistics();
            Map<String, Long> categoryStats = eventManagementService.getEventCategoryStatistics();
            Map<String, Long> monthlyStats = eventManagementService.getEventsByMonth(12);

            List<String> categories = eventManagementService.getAllCategories();

            model.addAttribute("title", "Event Management");
            model.addAttribute("eventsPage", eventsPage);
            model.addAttribute("searchDTO", searchDTO);
            model.addAttribute("categories", categories);
            model.addAttribute("eventStatuses", Events.EventStatus.values());

            model.addAttribute("totalEvents", totalEvents);
            model.addAttribute("ongoingEvents", ongoingEvents);
            model.addAttribute("cancelledEvents", cancelledEvents);
            model.addAttribute("completedEvents", totalEvents - ongoingEvents - cancelledEvents);

            model.addAttribute("statusStats", statusStats);
            model.addAttribute("categoryStats", categoryStats);
            model.addAttribute("monthlyStats", monthlyStats);

            return "admin/event-management";

        } catch (Exception e) {
            log.error("Error loading event management page", e);
            model.addAttribute("error", "An error occurred while loading data: " + e.getMessage());
            return "admin/event-management";
        }
    }

    @GetMapping("/details/{eventId}")
    public String eventDetails(
            @PathVariable Integer eventId,
            Model model) {

        log.info("Accessing event details for ID: {}", eventId);

        try {
            Optional<AdminEventDTO> eventOpt = eventManagementService.getEventDetails(eventId);

            if (eventOpt.isPresent()) {
                AdminEventDTO event = eventOpt.get();
                model.addAttribute("title", "Event Details - " + event.getTitle());
                model.addAttribute("event", event);
                return "admin/event-details";
            } else {
                model.addAttribute("error", "Event not found with ID: " + eventId);
                return "error/404";
            }

        } catch (Exception e) {
            log.error("Error loading event details for ID: {}", eventId, e);
            model.addAttribute("error", "An error occurred while loading event details: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/update-status/{eventId}")
    public String updateEventStatus(
            @PathVariable Integer eventId,
            @RequestParam Events.EventStatus status,
            RedirectAttributes redirectAttributes) {

        log.info("Updating event {} status to {}", eventId, status);

        try {
            boolean success = eventManagementService.updateEventStatus(eventId, status);

            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        "Event status updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Unable to update event status!");
            }

        } catch (Exception e) {
            log.error("Error updating event status", e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while updating status: " + e.getMessage());
        }

        return "redirect:/admin/event-management/details/" + eventId;
    }

    @PostMapping("/cancel/{eventId}")
    public String cancelEvent(
            @PathVariable Integer eventId,
            RedirectAttributes redirectAttributes) {

        log.info("Cancelling event: {}", eventId);

        try {
            boolean success = eventManagementService.deleteEvent(eventId);

            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        "Event has been successfully cancelled!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Unable to cancel event!");
            }

        } catch (Exception e) {
            log.error("Error cancelling event", e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while cancelling the event: " + e.getMessage());
        }

        return "redirect:/admin/event-management";
    }

    @PostMapping("/restore/{eventId}")
    public String restoreEvent(
            @PathVariable Integer eventId,
            RedirectAttributes redirectAttributes) {

        log.info("Restoring event: {}", eventId);

        try {
            boolean success = eventManagementService.restoreEvent(eventId);

            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        "Event has been successfully restored!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Unable to restore event!");
            }

        } catch (Exception e) {
            log.error("Error restoring event", e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while restoring the event: " + e.getMessage());
        }

        return "redirect:/admin/event-management/details/" + eventId;
    }

    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEventStatistics() {
        try {
            Map<String, Object> stats = Map.of(
                    "total", eventManagementService.getTotalEvents(),
                    "ongoing", eventManagementService.getOngoingEvents(),
                    "cancelled", eventManagementService.getCancelledEvents(),
                    "statusStats", eventManagementService.getEventStatusStatistics(),
                    "categoryStats", eventManagementService.getEventCategoryStatistics(),
                    "monthlyStats", eventManagementService.getEventsByMonth(6)
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting event statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Page<AdminEventDTO>> searchEvents(
            @ModelAttribute AdminEventSearchDTO searchDTO) {

        try {
            Page<AdminEventDTO> result = eventManagementService.searchAndFilterEvents(searchDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error searching events", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}