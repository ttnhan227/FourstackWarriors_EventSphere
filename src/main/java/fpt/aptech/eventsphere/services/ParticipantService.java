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
    Registrations registerForEvent(Integer eventId);
    
    void cancelRegistration(Integer eventId);
    
    boolean isUserRegisteredForEvent(Integer eventId);
    
    int getAvailableSeats(Integer eventId);
    
    List<Registrations> getUserRegistrations();
    
    Registrations getRegistrationForEvent(Integer eventId);
}
