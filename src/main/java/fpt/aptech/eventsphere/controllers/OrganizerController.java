package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.models.EventSeating;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.models.Venues;
import fpt.aptech.eventsphere.services.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    OrganizerService organizerService;
    @Autowired
    public OrganizerController(OrganizerService organizerService) {

        this.organizerService = organizerService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        model.addAttribute("list",  organizerService.findEventsByOrganizer(email));
        return "org/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Events event = new Events();
        event.setEventSeating(new EventSeating());
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

}
