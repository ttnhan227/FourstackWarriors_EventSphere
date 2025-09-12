package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.EventSeating;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.models.Venues;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.EventSeatingRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminUserRepository;
import fpt.aptech.eventsphere.repositories.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizerServiceImpl implements OrganizerService {

    EventRepository eventRepository;
    EventSeatingRepository eventSeatingRepository;
    VenueRepository  venueRepository;
    AdminUserRepository adminUserRepository;
    @Autowired
    public OrganizerServiceImpl(EventRepository eventRepository,
                                EventSeatingRepository eventSeatingRepository,
                                VenueRepository venueRepository,
                                AdminUserRepository adminUserRepository) {

        this.eventRepository = eventRepository;
        this.eventSeatingRepository = eventSeatingRepository;
        this.venueRepository = venueRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public Page<Events> findEventsByOrganizer(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findEventsByOrganizer(email, pageable);
    }

    @Override
    public Page<Events> findEventsByVenue(int id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findEventsByVenueId(id, pageable);
    }

    @Override
    public boolean addEvent(Events event) {
        eventRepository.save(event);
        return true;
    }

    @Override
    public Events findEventById(Integer id) {
        return eventRepository.findByEventId(id);
    }

    @Override
    public EventSeating findEventSeatingByEventId(Integer id) {
        return eventSeatingRepository.findByEventId(id);
    }

    @Override
    @Transactional
    public Events saveEvent(Events event) {
        try {
            return eventRepository.save(event);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Venues> findAllVenues() {
        return venueRepository.findAll();
    }

    @Override
    @Transactional
    public Venues saveVenue(Venues venue) {
        try {
            return venueRepository.save(venue);
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Users findOrganizerByEmail(String email) {
        if(adminUserRepository.findByEmail(email).isPresent()){
            return adminUserRepository.findByEmail(email).get();
        }
        return null;
    }
}
