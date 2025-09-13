package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EmailService emailService;

    @Scheduled(fixedRate = 2 * 60 * 1000) //every two minutes
    @Transactional
    public void sendEventReminders() {
        int hoursBefore = 1;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(hoursBefore);
        List<Events> events = eventRepository.findEventsStartingSoon(now, limit);

        for (Events event : events) {
            List<Registrations> registrations = eventRepository.findEventRegistrations(event.getEventId());
            List<String> emails = registrations.stream()
                    .map(r -> r.getStudent().getEmail())
                    .toList();

            if (!emails.isEmpty()) {
                String subject = "Reminder: " + event.getTitle() + " starts soon!";
                String body = String.format(
                        "Dear participant,%n%n" +
                                "This is a reminder that '%s' will start on %s at %s.%n%n" +
                                "We look forward to seeing you!%n%n" +
                                "Regards,%nEvent Team",
                        event.getTitle(),
                        event.getStartDate(),
                        event.getVenue().getName()
                );

                emailService.sendEmailToUsers(emails, subject, body);

                event.setReminderSent(true);
                eventRepository.save(event);
            }
        }
    }
}
