package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrganizerService {
    Page<Events> findEventsByOrganizer(String email, int page, int size);
    Page<Events> findEventsByVenue(int id, int page, int size);
    boolean addEvent(Events event);
    Events findEventById(Integer id);
    EventSeating findEventSeatingByEventId(Integer id);
    Events saveEvent(Events event);
    List<Venues> findAllVenues();
    Venues saveVenue(Venues venue);
    Users findOrganizerByEmail(String email);
    List<Registrations> findEventRegistration(int id);
    Events editEvent(Events event);
    boolean deleteEvent(Events event);
    List<Events> findUpcomingEvents(String email);
    List<Events> findPastEvents(String email);
    List<Events> findCurrentEvents(String email);
    
    Registrations confirmRegistration(int registrationId, int eventId);
    Registrations cancelRegistration(int registrationId, int eventId);
    Registrations updateRegistrationStatus(int registrationId, int eventId, String status);
    Registrations findRegistrationById(int registrationId);
}
