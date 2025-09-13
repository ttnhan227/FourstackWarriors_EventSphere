package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.models.Users;

import java.util.List;

public interface ParticipantService {
    // Get events that the current user has registered for
    List<Events> getUpcomingEvents();
    List<Events> getPastEvents();
    
    // Get all events (not just the ones the user registered for)
    List<Events> getAllUpcomingEvents();
    List<Events> getAllPastEvents();
    List<Events> getUpcomingEventsByCategory(String category);
    
    // User statistics
    int getTotalRegistrations();
    int getAttendedEventsCount();
    
    // User registration
    Users registerParticipant(ParticipantRegistrationDto registrationDto);
    
    // Event registration
    /**
     * Registers a user for an event with PENDING status
     * @param eventId The ID of the event to register for
     * @return The registration record with PENDING status
     */
    Registrations registerForEvent(Integer eventId);
    
    /**
     * Confirms a PENDING registration if seats are available
     * @param eventId The ID of the event to confirm registration for
     * @return The updated registration with CONFIRMED status if successful
     * @throws IllegalStateException if no seats are available or registration cannot be confirmed
     */
    Registrations confirmRegistration(Integer eventId);
    
    void cancelRegistration(Integer eventId);
    
    boolean isUserRegisteredForEvent(Integer eventId);
    
    int getAvailableSeats(Integer eventId);
    
    List<Registrations> getUserRegistrations();
    
    Registrations getRegistrationForEvent(Integer eventId);
    
    Registrations updateRegistrationStatus(Registrations registration, Registrations.RegistrationStatus newStatus, String emailType);
}
