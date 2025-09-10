package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilityServiceImpl implements UtilityService {

    UserRepository userRepository;
    EventRepository eventRepository;

    @Autowired
    public UtilityServiceImpl(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public int countUsers() {
        return userRepository.findAll().size();
    }

    @Override
    public int countEvents() {
        return eventRepository.findAll().size();
    }
}
