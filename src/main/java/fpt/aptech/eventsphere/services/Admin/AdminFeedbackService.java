package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.models.Feedback;
import fpt.aptech.eventsphere.repositories.admin.AdminFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminFeedbackService {

    @Autowired
    private AdminFeedbackRepository adminFeedbackRepository;
    public AdminFeedbackRepository getAdminFeedbackRepository() {
        return adminFeedbackRepository;
    }
    public List<Feedback> getAllFeedbacks() {
        return adminFeedbackRepository.findAll();
    }
}
