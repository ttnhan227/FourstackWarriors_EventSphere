package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.models.Events;
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
    
    // Registration
    Users registerParticipant(ParticipantRegistrationDto registrationDto);
}
