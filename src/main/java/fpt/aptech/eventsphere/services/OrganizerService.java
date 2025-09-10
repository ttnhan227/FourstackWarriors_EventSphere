package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;

import java.util.List;

public interface OrganizerService {
    public List<Events> findEventsByOrganizer(String email);
    public boolean addEvent(Events event);
}
