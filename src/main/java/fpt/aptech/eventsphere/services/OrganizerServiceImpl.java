package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizerServiceImpl implements OrganizerService {

    EventRepository eventRepository;
    @Autowired
    public OrganizerServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Events> findEventsByOrganizer(String email) {
        return eventRepository.findEventsByOrganizer(email);
    }

    @Override
    public boolean addEvent(Events event) {
        eventRepository.save(event);
        return true;
    }
}
