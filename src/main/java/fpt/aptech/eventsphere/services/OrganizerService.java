package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Events;

import java.util.List;

public interface OrganizerService {
    List<Events> findEventsByOrganizer(String email);
    boolean addEvent(Events event);
}
