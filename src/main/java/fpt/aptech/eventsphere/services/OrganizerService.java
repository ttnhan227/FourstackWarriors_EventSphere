package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.EventSeating;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.models.Venues;

import java.util.List;

public interface OrganizerService {
    List<Events> findEventsByOrganizer(String email);
    List<Events> findEventsByVenue(int id);
    boolean addEvent(Events event);
    Events  findEventById(Integer id);
    EventSeating findEventSeatingByEventId(Integer id);
    Events saveEvent(Events event);
    List<Venues> findAllVenues();
    Venues saveVenue(Venues venue);
    Users findOrganizerByEmail(String email);
}
