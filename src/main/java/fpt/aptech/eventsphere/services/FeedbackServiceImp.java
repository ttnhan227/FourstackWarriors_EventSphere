package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Feedback;
import fpt.aptech.eventsphere.repositories.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FeedbackServiceImp implements FeedbackService {
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    private static final int FEEDBACK_COOLDOWN_MINUTES = 5;

    @Override
    public List<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    @Override
    @Transactional
    public Feedback save(Feedback feedback) {
        log.info("Attempting to save feedback for user {} on event {}", 
                feedback.getStudent().getUserId(), feedback.getEvent().getEventId());
        
        try {
            Optional<Feedback> existingFeedback = feedbackRepository.findByEventIdAndUserId(
                    feedback.getEvent().getEventId(), 
                    feedback.getStudent().getUserId()
            );
            
            if (existingFeedback.isPresent()) {
                log.warn("User {} already provided feedback for event {}", 
                        feedback.getStudent().getUserId(), feedback.getEvent().getEventId());
                throw new RuntimeException("You have already provided feedback for this event!");
            }
            
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(FEEDBACK_COOLDOWN_MINUTES);
            int recentFeedbackCount = feedbackRepository.countRecentFeedbackByUser(
                    feedback.getStudent().getUserId(), fiveMinutesAgo
            );
            
            if (recentFeedbackCount > 0) {
                log.warn("User {} is trying to submit feedback too quickly. Recent feedback count: {}", 
                        feedback.getStudent().getUserId(), recentFeedbackCount);
                throw new RuntimeException("You can only submit feedback once every 5 minutes!");
            }
            
            if (feedback.getRating() < 1 || feedback.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5!");
            }
            
            feedback.setSubmittedOn(LocalDateTime.now());
            
            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback saved successfully with ID: {}", savedFeedback.getFeedbackId());
            
            return savedFeedback;
            
        } catch (Exception e) {
            log.error("Error saving feedback: ", e);
            throw new RuntimeException("Error saving...: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Feedback findById(Integer id) {
        log.info("Finding feedback by ID: {}", id);
        try {
            return feedbackRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Feedback not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error finding feedback by ID {}: ", id, e);
            throw new RuntimeException("Error finding feedback: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Feedback update(Feedback feedback) {
        log.info("Updating feedback with ID: {}", feedback.getFeedbackId());
        try {
            Feedback existingFeedback = findById(feedback.getFeedbackId());

            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(FEEDBACK_COOLDOWN_MINUTES);
            if (existingFeedback.getSubmittedOn().isAfter(fiveMinutesAgo)) {
                throw new RuntimeException("You can only update feedback 5 minutes after your last feedback!");
            }

            if (feedback.getRating() < 1 || feedback.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5!");
            }

            existingFeedback.setRating(feedback.getRating());
            existingFeedback.setComments(feedback.getComments());
            existingFeedback.setSubmittedOn(LocalDateTime.now());

            Feedback updatedFeedback = feedbackRepository.save(existingFeedback);
            log.info("Feedback updated successfully with ID: {}", updatedFeedback.getFeedbackId());

            return updatedFeedback;

        } catch (Exception e) {
            log.error("Error updating feedback: ", e);
            throw new RuntimeException("Error updating feedback: " + e.getMessage());
        }
    }

    public boolean canUserSubmitFeedback(Integer userId, Integer eventId) {
        try {
            Optional<Feedback> existingFeedback = feedbackRepository.findByEventIdAndUserId(eventId, userId);
            if (existingFeedback.isPresent()) {
                return false;
            }

            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(FEEDBACK_COOLDOWN_MINUTES);
            int recentFeedbackCount = feedbackRepository.countRecentFeedbackByUser(userId, fiveMinutesAgo);

            return recentFeedbackCount == 0;
        } catch (Exception e) {
            log.error("Error checking feedback eligibility: ", e);
            return false;
        }
    }

    public long getMinutesUntilNextFeedback(Integer userId) {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(FEEDBACK_COOLDOWN_MINUTES);
            var recentFeedbacks = feedbackRepository.findRecentFeedbackByUser(userId, fiveMinutesAgo);

            if (recentFeedbacks.isEmpty()) {
                return 0;
            }

            LocalDateTime latestFeedback = recentFeedbacks.get(0).getSubmittedOn();
            LocalDateTime canFeedbackAgain = latestFeedback.plusMinutes(FEEDBACK_COOLDOWN_MINUTES);

            if (LocalDateTime.now().isAfter(canFeedbackAgain)) {
                return 0;
            }

            return java.time.Duration.between(LocalDateTime.now(), canFeedbackAgain).toMinutes();
        } catch (Exception e) {
            log.error("Error calculating cooldown time: ", e);
            return 0;
        }
    }
}
