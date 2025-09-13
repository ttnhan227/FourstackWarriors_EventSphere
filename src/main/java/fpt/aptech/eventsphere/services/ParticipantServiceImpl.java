package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.models.*;
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
    private final EventSeatingRepository eventSeatingRepository;

    public ParticipantServiceImpl(ParticipantRepository participantRepository,
                            EventRepository eventRepository,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            EmailService emailService,
                            EventSeatingRepository eventSeatingRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.eventSeatingRepository = eventSeatingRepository;
    }

    // Removed duplicate getCurrentUser() method

    @Override
    @Transactional
    public Registrations registerForEvent(Integer eventId) {
        Users user = getCurrentUser();
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if user has an existing registration (including cancelled ones)
        return participantRepository.findRegistration(eventId, user.getUserId())
                .map(existingRegistration -> {
                    // If registration exists but is cancelled, update it to PENDING
                    if (existingRegistration.getStatus() == Registrations.RegistrationStatus.CANCELLED) {
                        logger.info("Reactivating cancelled registration for user {} to event {}", 
                            user.getUserId(), eventId);
                        // Just update to PENDING, don't try to confirm yet
                        existingRegistration.setStatus(Registrations.RegistrationStatus.PENDING);
                        existingRegistration.setRegisteredOn(java.time.LocalDateTime.now());
                        userRepository.save(user);
                        
                        // Return as PENDING - confirmation will happen in a separate step
                        return existingRegistration;
                    }
                    // If already registered, throw exception
                    throw new IllegalStateException("You are already registered for this event");
                })
                .orElseGet(() -> {
                    logger.info("Creating new registration for user {} to event {}", user.getUserId(), eventId);
                    
                    // Ensure EventSeating exists for this event first
                    ensureEventSeatingExists(event);
                    
                    // Create new registration with PENDING status first
                    Registrations newRegistration = new Registrations();
                    newRegistration.setEvent(event);
                    newRegistration.setStudent(user);
                    newRegistration.setStatus(Registrations.RegistrationStatus.PENDING);
                    newRegistration.setRegisteredOn(java.time.LocalDateTime.now());

                    // Add registration to user's registrations
                    user.getRegistrations().add(newRegistration);
                    
                    // Save the user (which will cascade save the registration)
                    userRepository.save(user);
                    logger.info("Created PENDING registration for user {} to event {}", 
                        user.getUserId(), eventId);
                    
                    // Send registration email with PENDING status
                    sendRegistrationEmail(user, event, "registration-pending");
                        
                    return newRegistration;
                });
    }
    
    private void ensureEventSeatingExists(Events event) {
        EventSeating seating = eventSeatingRepository.findByEventId(event.getEventId());
        if (seating == null) {
            logger.info("Creating new EventSeating for event {}", event.getEventId());
            seating = new EventSeating();
            seating.setEvent(event);
            seating.setTotalSeats(200); // Default value, adjust as needed
            seating.setSeatsBooked(0);
            seating.setWaitlistEnabled(false);
            eventSeatingRepository.save(seating);
        }
    }
    
    @Transactional
    public Registrations updateRegistrationStatus(Registrations registration, Registrations.RegistrationStatus newStatus, String emailType) {
        Integer eventId = registration.getEvent().getEventId();
        logger.info("Updating registration status for event {} from {} to {}", 
            eventId, registration.getStatus(), newStatus);
            
        Registrations.RegistrationStatus oldStatus = registration.getStatus();
        
        // If status is not changing, just return
        if (oldStatus == newStatus) {
            logger.info("Status not changed, returning existing registration");
            return registration;
        }

        // Handle seat count changes based on status transitions
        if (newStatus == Registrations.RegistrationStatus.CONFIRMED) {
            // Only increment if not already counted (coming from PENDING or CANCELLED)
            if (oldStatus != Registrations.RegistrationStatus.CONFIRMED) {
                logger.info("Changing to CONFIRMED from {} - increasing seat count", oldStatus);
                updateSeatCount(eventId, 1);
            }
        } else if (oldStatus == Registrations.RegistrationStatus.CONFIRMED) {
            // Only decrement if was previously CONFIRMED
            logger.info("Changing from CONFIRMED to {} - decreasing seat count", newStatus);
            updateSeatCount(eventId, -1);
        } else if (oldStatus == Registrations.RegistrationStatus.CANCELLED && 
                  newStatus == Registrations.RegistrationStatus.PENDING) {
            // When re-activating a CANCELLED registration to PENDING, no seat count change
            logger.info("Reactivating CANCELLED to PENDING - no seat count change");
        } else {
            logger.info("Status change from {} to {} - no seat count change needed", oldStatus, newStatus);
        }
        
        // Update the status
        registration.setStatus(newStatus);
        registration.setRegisteredOn(java.time.LocalDateTime.now());
        
        // Save the updated registration through the repository
        logger.info("Saving updated registration with status: {}", newStatus);
        userRepository.saveAndFlush(registration.getStudent());
        
        // Send email notification if needed
        if (emailType != null) {
            logger.info("Sending {} email for event {}", emailType, eventId);
            sendRegistrationEmail(registration.getStudent(), registration.getEvent(), emailType);
        }
        
        return registration;
    }
    
    private void updateSeatCount(Integer eventId, int change) {
        logger.info("Updating seat count for event {} with change: {}", eventId, change);
        
        // Get or create EventSeating
        EventSeating seating = eventSeatingRepository.findByEventId(eventId);
        if (seating == null) {
            logger.info("No EventSeating found for event {}, creating a new one", eventId);
            Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
                
            seating = new EventSeating();
            seating.setEvent(event);
            seating.setTotalSeats(200); // Default value, adjust as needed
            seating.setSeatsBooked(0);
            seating.setWaitlistEnabled(false);
            seating = eventSeatingRepository.save(seating);
            logger.info("Created new EventSeating for event {}", eventId);
        }
        
        // Calculate new seats booked
        int currentBooked = seating.getSeatsBooked();
        int newSeatsBooked = currentBooked + change;
        
        logger.info("Current state - Total: {}, Booked: {}, Available: {}", 
            seating.getTotalSeats(), currentBooked, seating.getAvailableSeat());
        logger.info("Requested change: {}, New booked seats will be: {}", change, newSeatsBooked);
        
        // Validate seat availability
        if (newSeatsBooked > seating.getTotalSeats()) {
            logger.warn("Cannot book more seats than available. Requested: {}, Available: {}", 
                newSeatsBooked, seating.getTotalSeats());
            throw new IllegalStateException("No seats available");
        }
        
        if (newSeatsBooked < 0) {
            logger.warn("Cannot have negative booked seats. Requested: {}", newSeatsBooked);
            throw new IllegalStateException("Invalid seat count");
        }
        
        // Update the seat count
        seating.setSeatsBooked(newSeatsBooked);
        eventSeatingRepository.save(seating);
        logger.info("Seat count updated - Total: {}, Booked: {}, Available: {}", 
            seating.getTotalSeats(), newSeatsBooked, (seating.getTotalSeats() - newSeatsBooked));
    }

    @Override
    @Transactional
    public Registrations confirmRegistration(Integer eventId) {
        Users user = getCurrentUser();
        Registrations registration = participantRepository.findRegistration(eventId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Registration not found"));
                
        // Only allow confirming PENDING registrations
        if (registration.getStatus() != Registrations.RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING registrations can be confirmed");
        }
        
        // This will handle seat availability check and update
        return updateRegistrationStatus(registration, 
            Registrations.RegistrationStatus.CONFIRMED, 
            "confirmation"
        );
    }
    
    @Override
    @Transactional
    public void cancelRegistration(Integer eventId) {
        Users user = getCurrentUser();
        Registrations registration = participantRepository.findRegistration(eventId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        
        logger.info("Canceling registration for user {} to event {}", user.getUserId(), eventId);
        
        // Only allow cancellation if event hasn't started yet
        if (registration.getEvent().getStartDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel registration after event has started");
        }
        
        // This will handle seat count updates if needed
        updateRegistrationStatus(registration, Registrations.RegistrationStatus.CANCELLED, "cancellation");
        
        logger.info("Successfully cancelled registration for user {} to event {}", user.getUserId(), eventId);
    }

    @Override
    public boolean isUserRegisteredForEvent(Integer eventId) {
        Users user = getCurrentUser();
        return participantRepository.findRegistration(eventId, user.getUserId())
                .map(registration -> registration.getStatus() == Registrations.RegistrationStatus.CONFIRMED || 
                                    registration.getStatus() == Registrations.RegistrationStatus.PENDING)
                .orElse(false);
    }

    @Override
    public int getAvailableSeats(Integer eventId) {
        logger.info("Getting available seats for event {}", eventId);
        
        // First try to get existing seating
        EventSeating seating = eventSeatingRepository.findByEventId(eventId);
        
        // If no seating exists, create one
        if (seating == null) {
            logger.info("No EventSeating found for event {}, creating a default one", eventId);
            Events event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                logger.warn("Event {} not found, returning 0 available seats", eventId);
                return 0;
            }
            
            seating = new EventSeating();
            seating.setEvent(event);
            seating.setTotalSeats(200); // Default value
            seating.setSeatsBooked(0);
            seating.setWaitlistEnabled(false);
            seating = eventSeatingRepository.save(seating);
            logger.info("Created default EventSeating for event {}", eventId);
        }
        
        int available = seating.getAvailableSeat();
        logger.info("Available seats for event {}: {}", eventId, available);
        return available;
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
