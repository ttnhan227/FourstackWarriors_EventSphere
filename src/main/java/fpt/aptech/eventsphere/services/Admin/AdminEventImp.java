package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public class AdminEventImp implements AdminEventService{

    @Autowired
    private AdminEventRepository repo;

    @Override
    public List<Events> findAll() {
        return repo.findAll();
    }
}
