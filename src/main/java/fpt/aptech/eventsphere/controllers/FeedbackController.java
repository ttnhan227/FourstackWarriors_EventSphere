package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.services.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    @Autowired
    private FeedbackService fbService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping
    public String feedbackList(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            
            Users currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Events> completedEvents = getCompletedEventsForUser(currentUser.getUserId());
            
            Map<Integer, Feedback> existingFeedbacks = new HashMap<>();
            List<Feedback> userFeedbacks = feedbackRepository.findByStudentUserId(currentUser.getUserId());
            for (Feedback feedback : userFeedbacks) {
                existingFeedbacks.put(feedback.getEvent().getEventId(), feedback);
            }

            model.addAttribute("title", "My Feedback");
            model.addAttribute("completedEvents", completedEvents);
            model.addAttribute("existingFeedbacks", existingFeedbacks);
            model.addAttribute("currentUser", currentUser);

            return "feedback/list";

        } catch (Exception e) {
            log.error("Error loading feedback list: ", e);
            model.addAttribute("error", "Error loading feedback list: " + e.getMessage());
            return "feedback/list";
        }
    }

    @GetMapping("/event/{eventId}")
    public String feedbackForm(@PathVariable Integer eventId, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            
            Users currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Events event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            if (!event.isCompleted()) {
                model.addAttribute("error", "You can only provide feedback for completed events");
                return "redirect:/feedback";
            }

            if (!hasUserAttendedEvent(currentUser.getUserId(), eventId)) {
                model.addAttribute("error", "You can only provide feedback for events you have attended");
                return "redirect:/feedback";
            }

            Optional<Feedback> existingFeedback = feedbackRepository.findByEventIdAndUserId(eventId, currentUser.getUserId());
            
            model.addAttribute("title", "Feedback for " + event.getTitle());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("existingFeedback", existingFeedback.orElse(null));
            model.addAttribute("isEdit", existingFeedback.isPresent());

            return "feedback/form";

        } catch (Exception e) {
            log.error("Error loading feedback form: ", e);
            model.addAttribute("error", "Error loading feedback form: " + e.getMessage());
            return "redirect:/feedback";
        }
    }

    @PostMapping("/submit")
    public String submitFeedback(
            @RequestParam Integer eventId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comments,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            
            Users currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Events event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            if (!event.isCompleted()) {
                redirectAttributes.addFlashAttribute("error", "You can only provide feedback for completed events");
                return "redirect:/feedback";
            }

            if (!hasUserAttendedEvent(currentUser.getUserId(), eventId)) {
                redirectAttributes.addFlashAttribute("error", "You can only provide feedback for events you have attended");
                return "redirect:/feedback";
            }

            if (!fbService.canUserSubmitFeedback(currentUser.getUserId(), eventId)) {
                long minutesLeft = fbService.getMinutesUntilNextFeedback(currentUser.getUserId());
                if (minutesLeft > 0) {
                    redirectAttributes.addFlashAttribute("error", 
                        "You can only submit feedback once every 5 minutes. Please wait " + minutesLeft + " more minutes.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You have already provided feedback for this event");
                }
                return "redirect:/feedback";
            }

            Feedback feedback = new Feedback();
            feedback.setEvent(event);
            feedback.setStudent(currentUser);
            feedback.setRating(rating);
            feedback.setComments(comments);

            fbService.save(feedback);

            redirectAttributes.addFlashAttribute("success", "Your feedback has been submitted successfully!");
            return "redirect:/feedback";

        } catch (Exception e) {
            log.error("Error submitting feedback: ", e);
            redirectAttributes.addFlashAttribute("error", "Error submitting feedback: " + e.getMessage());
            return "redirect:/feedback/event/" + eventId;
        }
    }

    @PostMapping("/update/{feedbackId}")
    public String updateFeedback(
            @PathVariable Integer feedbackId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comments,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            
            Users currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Feedback existingFeedback = fbService.findById(feedbackId);

            if (existingFeedback.getStudent().getUserId() != currentUser.getUserId()) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own feedback");
                return "redirect:/feedback";
            }


            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            if (existingFeedback.getSubmittedOn().isAfter(fiveMinutesAgo)) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), 
                    existingFeedback.getSubmittedOn().plusMinutes(5)).toMinutes();
                redirectAttributes.addFlashAttribute("error", 
                    "You can only edit feedback 5 minutes after submission. Please wait " + minutesLeft + " more minutes.");
                return "redirect:/feedback/event/" + existingFeedback.getEvent().getEventId();
            }

            existingFeedback.setRating(rating);
            existingFeedback.setComments(comments);

            fbService.update(existingFeedback);

            redirectAttributes.addFlashAttribute("success", "Your feedback has been updated successfully!");
            return "redirect:/feedback";

        } catch (Exception e) {
            log.error("Error updating feedback: ", e);
            redirectAttributes.addFlashAttribute("error", "Error updating feedback: " + e.getMessage());
            return "redirect:/feedback";
        }
    }

    @GetMapping("/api/check/{eventId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFeedbackStatus(@PathVariable Integer eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            
            Users currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean canFeedback = fbService.canUserSubmitFeedback(currentUser.getUserId(), eventId);
            long minutesLeft = fbService.getMinutesUntilNextFeedback(currentUser.getUserId());

            response.put("canFeedback", canFeedback);
            response.put("minutesLeft", minutesLeft);

            Optional<Feedback> existingFeedback = feedbackRepository.findByEventIdAndUserId(eventId, currentUser.getUserId());
            response.put("hasFeedback", existingFeedback.isPresent());

            if (existingFeedback.isPresent()) {
                response.put("canEdit", LocalDateTime.now().isAfter(
                    existingFeedback.get().getSubmittedOn().plusMinutes(5)));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking feedback status: ", e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private List<Events> getCompletedEventsForUser(Integer userId) {
        return eventRepository.findAll().stream()
            .filter(event -> event.isCompleted() && hasUserAttendedEvent(userId, event.getEventId()))
            .toList();
    }

    private boolean hasUserAttendedEvent(Integer userId, Integer eventId) {
        return eventRepository.findEventRegistrations(eventId).stream()
                .anyMatch(registration -> registration.getStudent().getUserId() == userId
                        && registration.getStatus() == Registrations.RegistrationStatus.CONFIRMED);
    }

}
