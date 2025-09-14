package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Feedback;

import java.util.List;

public interface FeedbackService {
    List<Feedback> findAll();
    Feedback save(Feedback feedback);
    Feedback findById(Integer id);
    Feedback update(Feedback feedback);
    boolean canUserSubmitFeedback(Integer userId, Integer eventId);
    long getMinutesUntilNextFeedback(Integer userId);


}
