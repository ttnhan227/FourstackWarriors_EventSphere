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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private final OrganizerService organizerService;
    private final RegistrationMapper registrationMapper = new RegistrationMapper();
    private final String UPLOAD_DIR = "src/main/resources/static/images/events";

    @Autowired
    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "4") int size,
                        @RequestParam(defaultValue = "current") String tab,
                        Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Page<Events> events = organizerService.findEventsByOrganizer(email, page, size);
        List<Events> upcomingEvents = organizerService.findUpcomingEvents(email);
        List<Events> pastEvents = organizerService.findPastEvents(email);
        List<Events> currentEvents = organizerService.findCurrentEvents(email);
        model.addAttribute("eventPage", events);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("currentEvents", currentEvents);
        model.addAttribute("activeTab", tab);
        return "org/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Events event = new Events();
        EventSeating seating = new EventSeating();
        event.setActivities(new ArrayList<>());
        seating.setWaitlistEnabled(true);
        event.setEventSeating(seating);
        model.addAttribute("event", event);
        model.addAttribute("venue", organizerService.findAllVenues());
        model.addAttribute("hosts", organizerService.findAllHosts());
        return "org/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("event") Events event,
                         BindingResult bindingResult,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("venue", organizerService.findAllVenues());
            model.addAttribute("hosts", organizerService.findAllHosts());
            return "org/create";
        }

        // Handle image upload
        try {
            if (!imageFile.isEmpty()) {
                String imageUrl = handleImageUpload(imageFile);
                event.setImageUrl(imageUrl);
            }
        } catch (Exception e) {
            model.addAttribute("venue", organizerService.findAllVenues());
            model.addAttribute("error", e.getMessage());
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
        if (bindingResult.hasErrors()) {
            return "org/createVenue";
        }

        organizerService.saveVenue(venue);
        return "redirect:/organizer/index";
    }

    @GetMapping("/detail/{id}")
    public String showEventDetail(@PathVariable int id,
                                  @RequestParam(defaultValue = "details") String tab,
                                  Model model,
                                  @ModelAttribute("errorMessage") String errorMessage,
                                  @ModelAttribute("successMessage") String successMessage) {
        Events event = organizerService.findEventById(id);
        EventSeating seating = organizerService.findEventSeatingByEventId(id);
        List<Registrations> registrationsList = organizerService.findEventRegistration(id);
        List<RegistrationDTO> regDTOList = registrationMapper.toDTOList(registrationsList);
        model.addAttribute("event", event);
        model.addAttribute("registration", regDTOList);
        model.addAttribute("statuses", Registrations.RegistrationStatus.values());
        model.addAttribute("availableSeat", seating.getAvailableSeat());
        model.addAttribute("activeTab", tab);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }
        return "org/detail";
    }

    @PostMapping("/registrations/{registrationId}/confirm")
    public String confirmRegistration(@PathVariable int registrationId,
                                    @RequestParam("eventId") int eventId) {
        organizerService.confirmRegistration(registrationId, eventId);
        return "redirect:/organizer/detail/" + eventId;
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    public String cancelRegistration(@PathVariable int registrationId,
                                   @RequestParam("eventId") int eventId) {
        organizerService.cancelRegistration(registrationId, eventId);
        return "redirect:/organizer/detail/" + eventId;
    }

    @PostMapping("/registrations/{registrationId}/status")
    public String updateRegistrationStatus(@PathVariable int registrationId,
                                        @RequestParam("eventId") int eventId,
                                        @RequestParam("status") String status) {
        organizerService.updateRegistrationStatus(registrationId, eventId, status);
        return "redirect:/organizer/detail/" + eventId;
    }

    @GetMapping("/edit/{id}")
    public String showEventEdit(@PathVariable int id, Model model) {
        model.addAttribute("venue", organizerService.findAllVenues());
        Events event = organizerService.findEventById(id);
        model.addAttribute("event", event);
        return "org/edit";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute Events event,
                       BindingResult bindingResult,
                       @RequestParam("imageFile") MultipartFile imageFile,
                       Model model) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error ->
                    System.out.println("Error: " + error.getDefaultMessage()));
            model.addAttribute("venue", organizerService.findAllVenues());
            model.addAttribute("event", event);
            return "org/edit";
        }

        // Handle image upload
        try {
            if (!imageFile.isEmpty()) {
                String imageUrl = handleImageUpload(imageFile);
                event.setImageUrl(imageUrl);
            }
        } catch (Exception e) {
            model.addAttribute("venue", organizerService.findAllVenues());
            model.addAttribute("error", e.getMessage());
            return "org/edit";
        }

        organizerService.editEvent(event);
        return "redirect:/organizer/index";
    }

    @PostMapping("/confirmRegistration/{registrationId}")
    public String confirmRegistration(@PathVariable int registrationId, RedirectAttributes redirectAttributes) {
        try {
            Registrations reg = organizerService.findRegistrationById(registrationId);
            organizerService.confirmRegistration(registrationId);
            redirectAttributes.addFlashAttribute("successMessage", "Registration confirmed successfully.");
            redirectAttributes.addFlashAttribute("errorMessage", "");
            return "redirect:/organizer/detail/" + reg.getEvent().getEventId() + "?tab=registration";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("successMessage", "");

            Registrations reg = organizerService.findRegistrationById(registrationId);
            return "redirect:/organizer/detail/" + reg.getEvent().getEventId() + "?tab=registration";
        }
    }

    @PostMapping("/cancelRegistration/{registrationId}")
    public String cancelRegistration(@PathVariable int registrationId, RedirectAttributes redirectAttributes) {
        try {
            Registrations reg = organizerService.findRegistrationById(registrationId);
            organizerService.cancelRegistration(registrationId);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled successfully.");
            redirectAttributes.addFlashAttribute("errorMessage", "");
            return "redirect:/organizer/detail/" + reg.getEvent().getEventId() + "?tab=registration";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("successMessage", "");
            Registrations reg = organizerService.findRegistrationById(registrationId);
            return "redirect:/organizer/detail/" + reg.getEvent().getEventId() + "?tab=registration";
        }
    }

    @GetMapping("/registration/detail/{registrationId}")
    public String registrationDetail(@PathVariable int registrationId, Model model) {
        Registrations reg = organizerService.findRegistrationById(registrationId);
        model.addAttribute("registration", reg);
        model.addAttribute("event", reg.getEvent());
        model.addAttribute("student", reg.getStudent());
        return "reg/detail";
    }

    @GetMapping("/createHost")
    public String showHostCreateForm(Model model) {
        Host host = new Host();
        model.addAttribute("host", host);
        return "org/createHost";
    }

    @PostMapping("/createHost")
    public String createHost(@Valid @ModelAttribute("host") Host host,
                             BindingResult bindingResult,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "org/createHost";
        }

        try {
            if (!imageFile.isEmpty()) {
                String imageUrl = handleImageUpload(imageFile);
                host.setImageUrl(imageUrl);
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "org/createHost";
        }

        organizerService.saveHost(host);
        return "redirect:/organizer/index";
    }

    private String handleImageUpload(MultipartFile imageFile) throws Exception {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new Exception("No file selected for upload.");
        }

        String originalFileName = imageFile.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new Exception("Invalid file name.");
        }

        String contentType = imageFile.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new Exception("Only JPEG, PNG, and GIF images are allowed.");
        }

        long maxFileSize = 50 * 1024 * 1024; // 50MB in bytes
        if (imageFile.getSize() > maxFileSize) {
            throw new Exception("Image file size exceeds 50MB limit.");
        }

        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, imageFile.getBytes());

        return "/images/events/" + fileName;
    }
}