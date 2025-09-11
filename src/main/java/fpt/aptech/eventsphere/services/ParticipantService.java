package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;

import java.util.List;

public interface ParticipantService {
    List<Events> getUpcomingEvents();
    List<Events> getPastEvents();
    int getTotalRegistrations();
    int getAttendedEventsCount();
    Users registerParticipant(ParticipantRegistrationDto registrationDto);
}
