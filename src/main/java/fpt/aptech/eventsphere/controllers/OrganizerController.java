package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.RegistrationDTO;
import fpt.aptech.eventsphere.mappers.RegistrationMapper;
import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.services.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    OrganizerService organizerService;
    RegistrationMapper registrationMapper = new RegistrationMapper();
    @Autowired
    public OrganizerController(OrganizerService organizerService) {

        this.organizerService = organizerService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "4") int size,
                        Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Page<Events> events = organizerService.findEventsByOrganizer(email, page, size);
        model.addAttribute("eventPage", events);
        return "org/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Events event = new Events();
        EventSeating seating = new EventSeating();
        seating.setWaitlistEnabled(false);
        event.setEventSeating(seating);
        model.addAttribute("event", event);
        model.addAttribute("venue", organizerService.findAllVenues());
        return "org/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("event") Events event,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("venue", organizerService.findAllVenues());
            return "org/create";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users organizer = organizerService.findOrganizerByEmail(email);
        event.setOrganizer(organizer);
        organizerService.saveEvent(event);
        return "redirect:/organizer/index";
    }

    @GetMapping("/createVenue")
    public String showVenueCreateForm(Model model) {
        Venues venue = new Venues();
        model.addAttribute("venue", venue);
        return "org/createVenue";
    }

    @PostMapping("/createVenue")
    public String createVenue(@Valid @ModelAttribute("venue") Venues venue,
                              BindingResult bindingResult,
                              Model model) {
        if(bindingResult.hasErrors()) {
            return "org/createVenue";
        }

        organizerService.saveVenue(venue);
        return "redirect:/organizer/index";
    }

    @GetMapping("/detail/{id}")
    public String showEventDetail(@PathVariable int id, Model model) {
        Events event = organizerService.findEventById(id);
        List<Registrations> registrationsList = organizerService.findEventRegistration(id);
        List<RegistrationDTO> regDTOList = registrationMapper.toDTOList(registrationsList);
        model.addAttribute("event", event);
        model.addAttribute("registration", regDTOList);
        return "org/detail";
    }
}
