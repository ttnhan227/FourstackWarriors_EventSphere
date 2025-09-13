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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        seating.setWaitlistEnabled(false);
        event.setEventSeating(seating);
        model.addAttribute("event", event);
        model.addAttribute("venue", organizerService.findAllVenues());
        return "org/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("event") Events event,
                         BindingResult bindingResult,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("venue", organizerService.findAllVenues());
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
    public String showEventDetail(@PathVariable int id, Model model) {
        Events event = organizerService.findEventById(id);
        List<Registrations> registrationsList = organizerService.findEventRegistration(id);
        List<RegistrationDTO> regDTOList = registrationMapper.toDTOList(registrationsList);
        model.addAttribute("event", event);
        model.addAttribute("registration", regDTOList);
        return "org/detail";
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

    private String handleImageUpload(MultipartFile imageFile) throws Exception {
        // Validate file is not null or empty
        if (imageFile == null || imageFile.isEmpty()) {
            throw new Exception("No file selected for upload.");
        }

        // Validate file name
        String originalFileName = imageFile.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new Exception("Invalid file name.");
        }

        // Validate MIME type
        String contentType = imageFile.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new Exception("Only JPEG, PNG, and GIF images are allowed.");
        }

        // Validate file size (e.g., max 50MB)
        long maxFileSize = 50 * 1024 * 1024; // 50MB in bytes
        if (imageFile.getSize() > maxFileSize) {
            throw new Exception("Image file size exceeds 100MB limit.");
        }

        // Generate unique file name
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Ensure directory exists
        Files.createDirectories(filePath.getParent());

        // Save file
        Files.write(filePath, imageFile.getBytes());

        // Return the relative URL
        return "/images/events/" + fileName;
    }
}