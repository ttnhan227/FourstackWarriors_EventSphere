package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.models.Events;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;


public interface AdminEventService {
    List<Events> findAll();
}
