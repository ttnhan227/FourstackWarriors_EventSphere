package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.models.Roles;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.ParticipantRepository;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserDetailsRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class ParticipantServiceImpl implements ParticipantService {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantServiceImpl.class);

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public ParticipantServiceImpl(ParticipantRepository participantRepository,
                            EventRepository eventRepository,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            EmailService emailService) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Removed duplicate getCurrentUser() method

    @Override
    public Registrations registerForEvent(Integer eventId) {
        Users user = getCurrentUser();
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if user has an existing registration (including cancelled ones)
        return participantRepository.findRegistration(eventId, user.getUserId())
                .map(existingRegistration -> {
                    // If registration exists but is cancelled, update it to CONFIRMED
                    if (existingRegistration.getStatus() == Registrations.RegistrationStatus.CANCELLED) {
                        existingRegistration.setStatus(Registrations.RegistrationStatus.CONFIRMED);
                        existingRegistration.setRegisteredOn(java.time.LocalDateTime.now());
                        userRepository.save(user);
                        sendRegistrationEmail(user, event, "re-registration");
                        return existingRegistration;
                    }
                    // If already confirmed, throw exception
                    throw new IllegalStateException("You are already registered for this event");
                })
                .orElseGet(() -> {
                    // No existing registration, create a new one
                    Registrations newRegistration = new Registrations();
                    newRegistration.setEvent(event);
                    newRegistration.setStudent(user);
                    newRegistration.setStatus(Registrations.RegistrationStatus.CONFIRMED);
                    newRegistration.setRegisteredOn(java.time.LocalDateTime.now());

                    // Add registration to user's registrations and save
                    user.getRegistrations().add(newRegistration);
                    userRepository.save(user);
                    
                    sendRegistrationEmail(user, event, "registration");
                    return newRegistration;
                });
    }

    @Override
    @Transactional
    public void cancelRegistration(Integer eventId) {
        Users user = getCurrentUser();
        Registrations registration = participantRepository.findRegistration(eventId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        // Only allow cancellation if event hasn't started yet
        if (registration.getEvent().getStartDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel registration after event has started");
        }
        
        // Update status to CANCELLED instead of deleting
        registration.setStatus(Registrations.RegistrationStatus.CANCELLED);
        // Save through the user's registration collection to maintain consistency
        user.getRegistrations().removeIf(r -> r.getRegistrationId() == registration.getRegistrationId());
        user.getRegistrations().add(registration);
        userRepository.save(user);
    }

    @Override
    public boolean isUserRegisteredForEvent(Integer eventId) {
        Users user = getCurrentUser();
        return participantRepository.findRegistration(eventId, user.getUserId())
                .map(registration -> registration.getStatus() == Registrations.RegistrationStatus.CONFIRMED)
                .orElse(false);
    }

    @Override
    public int getAvailableSeats(Integer eventId) {
        // For now, assume unlimited seats since maxParticipants is not in the Events model
        // TODO: Add maxParticipants field to Events model if needed
        return Integer.MAX_VALUE;
    }
    
    private void sendRegistrationEmail(Users user, Events event, String emailType) {
        try {
            String subject = "EventSphere: " + event.getTitle() + " - Registration " + 
                ("registration".equals(emailType) ? "Confirmed" : "Updated");
            
            String body = String.format("""
                <html>
                <body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;\">
                    <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px;\">
                        <h2 style=\"color: #2c3e50;\">Event Registration %s</h2>
                        <p>Dear %s,</p>
                        <p>This email confirms your %s for the following event:</p>
                        
                        <div style=\"background: white; padding: 15px; border-radius: 5px; margin: 15px 0;\">
                            <h3 style=\"margin-top: 0; color: #2c3e50;\">%s</h3>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            <p><strong>Location:</strong> %s</p>
                        </div>
                        
                        <p>We look forward to seeing you there!</p>
                        
                        <p>Best regards,<br>The EventSphere Team</p>
                        
                        <div style=\"margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee; font-size: 12px; color: #777;\">
                            <p>This is an automated message. Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                "registration".equals(emailType) ? "Confirmation" : "Update",
                user.getEmail(),
                "registration".equals(emailType) ? "registration" : "registration update",
                event.getTitle(),
                event.getStartDate().toLocalDate(),
                event.getStartDate().toLocalTime(),
                event.getVenue() != null ? event.getVenue().getName() : "To be announced"
            );

            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send registration email", e);
        }
    }

    @Override
    public List<Registrations> getUserRegistrations() {
        Users user = getCurrentUser();
        return participantRepository.findUpcomingRegistrations(user.getUserId());
    }
    
    @Override
    public Registrations getRegistrationForEvent(Integer eventId) {
        Users user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.findRegistration(eventId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Registration not found for this event"));
    }

    @Override
    public List<Events> getUpcomingEvents() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.findUpcomingRegisteredEvents(currentUser.getUserId());
    }

    @Override
    public List<Events> getPastEvents() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.findPastRegisteredEvents(currentUser.getUserId());
    }
    
    @Override
    public List<Events> getAllUpcomingEvents() {
        logger.debug("Fetching all upcoming events");
        List<Events> events = eventRepository.findAllUpcomingEvents();
        logger.debug("Found {} upcoming events", events.size());
        return events;
    }
    
    @Override
    public List<Events> getAllPastEvents() {
        logger.debug("Fetching all past events");
        List<Events> events = eventRepository.findAllPastEvents();
        logger.debug("Found {} past events", events.size());
        return events;
    }
    
    @Override
    public List<Events> getUpcomingEventsByCategory(String category) {
        logger.debug("Fetching upcoming events for category: {}", category);
        List<Events> events = eventRepository.findUpcomingEventsByCategory(category);
        logger.debug("Found {} events for category: {}", events.size(), category);
        return events;
    }

    @Override
    public int getTotalRegistrations() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.countRegistrationsByUserId(currentUser.getUserId());
    }

    @Override
    public int getAttendedEventsCount() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.countAttendedEvents(currentUser.getUserId());
    }

    @Override
    public Users registerParticipant(ParticipantRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        Users user = new Users();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setActive(true);
        
        // Set participant role
        Roles participantRole = roleRepository.findByRoleName("PARTICIPANT")
                .orElseThrow(() -> new IllegalStateException("PARTICIPANT role not found"));
        user.getRoles().add(participantRole);
        
        Users savedUser = userRepository.save(user);
        
        // Create user details
        UserDetails userDetails = new UserDetails();
        userDetails.setUser(savedUser);
        userDetails.setFullName(registrationDto.getFullName());
        userDetails.setPhone(registrationDto.getPhone());
        userDetails.setDepartment(registrationDto.getDepartment());
        userDetails.setEnrollmentNo(registrationDto.getStudentId());
        userDetailsRepository.save(userDetails);
        
        return savedUser;
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
