package fpt.aptech.eventsphere.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.EventSeatingRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import fpt.aptech.eventsphere.repositories.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service    
public class OrganizerServiceImpl implements OrganizerService {

    EventRepository eventRepository;
    EventSeatingRepository eventSeatingRepository;
    UserRepository userRepository;
    VenueRepository venueRepository;
    @Autowired
    EmailServiceImpl emailServiceImpl;
    @Autowired
    public OrganizerServiceImpl(EventRepository eventRepository,
                                VenueRepository venueRepository,
                                EventSeatingRepository eventSeatingRepository,
                                UserRepository userRepository) {

        this.eventRepository = eventRepository;
        this.eventSeatingRepository = eventSeatingRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
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
            Events savedEvent = eventRepository.save(event);
            String eventUrl = "http://localhost:9999/organizer/detail/" + savedEvent.getEventId();
            byte[] qrCode = generateQRCode(eventUrl, 250, 250);
            String subject = "Event Created: " + savedEvent.getTitle();
            String body = "Your event has been successfully created. The QR code for the event details is attached.";

            emailServiceImpl.sendEmailWithAttachment(List.of(savedEvent.getOrganizer().getEmail()), subject, body, qrCode, "event_qr.png");
            return savedEvent;
        } catch (IOException | WriterException e) {
            throw new RuntimeException("Failed to generate QR code or send email", e);
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
        if(userRepository.findByEmail(email).isPresent()){
            return userRepository.findByEmail(email).get();
        }
        return null;
    }

    @Override
    public List<Registrations> findEventRegistration(int id) {
        return eventRepository.findEventRegistrations(id);
    }

    @Override
    @Transactional
    public Events editEvent(Events formEvent) {
        Events existing = eventRepository.findById(formEvent.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        // Update event fields
        existing.setTitle(formEvent.getTitle());
        existing.setCategory(formEvent.getCategory());
        existing.setDescription(formEvent.getDescription());
        existing.setStartDate(formEvent.getStartDate());
        existing.setEndDate(formEvent.getEndDate());
        existing.setVenue(formEvent.getVenue());
        existing.setImageUrl(formEvent.getImageUrl());

        if (existing.getEventSeating() != null && formEvent.getEventSeating() != null) {
            existing.getEventSeating().setTotalSeats(formEvent.getEventSeating().getTotalSeats());
            existing.getEventSeating().setWaitlistEnabled(formEvent.getEventSeating().isWaitlistEnabled());
        }
        //save
        eventRepository.save(existing);

        //get venue for email sending
        Venues venue = venueRepository.findById(existing.getVenue().getVenueId()).get();

        //Send email to all registrants
        // Fetch all registrations for this event
        List<Registrations> registrations = findEventRegistration(existing.getEventId());

        // Collect emails
        List<String> emails = registrations.stream()
                .map(r -> r.getStudent().getEmail())
                .toList();

        if (!emails.isEmpty()) {
            String subject = "Update: " + existing.getTitle();

            String body = String.format(
                    "Dear participant,%n%n" +
                            "The event '%s' has been updated.%n%n" +
                            "Here are the latest details:%n" +
                            "Start: %s%n" +
                            "End: %s%n" +
                            "Location: %s%n" +
                            "Venue Address: %s%n%n" +
                            "Please check the event page for more information.%n%n" +
                            "Regards,%nEvent Team",
                    existing.getTitle(),
                    existing.getStartDate(),
                    existing.getEndDate(),
                    venue.getName(),
                    venue.getAddress()
            );

            emailServiceImpl.sendEmailToUsers(emails, subject, body);
        }

        try {
            String eventUrl = "http://localhost:9999/organizer/detail/" + existing.getEventId();
            byte[] qrCode = generateQRCode(eventUrl, 250, 250);
            String subject = "Update: Your Event " + existing.getTitle() + " Has Been Modified";
            String body = "The details for your event have been updated. A new QR code is attached.";

            emailServiceImpl.sendEmailWithAttachment(List.of(existing.getOrganizer().getEmail()), subject, body, qrCode, "updated_event_qr.png");
        } catch (IOException | WriterException e) {
            throw new RuntimeException("Failed to generate QR code or send email for edit", e);
        }

        return existing;
    }

    @Override
    public boolean deleteEvent(Events event) {
        eventRepository.delete(event);
        return true;
    }

    @Override
    public List<Events> findUpcomingEvents(String email) {
        return eventRepository.findUpcomingEventsByOrganizer(email);
    }

    @Override
    public List<Events> findPastEvents(String email) {
        return eventRepository.findPastEventsByOrganizer(email);
    }

    @Override
    public List<Events> findCurrentEvents(String email) {
        return eventRepository.findCurrentEventsByOrganizer(email);
    }
    
    @Override
    public Registrations findRegistrationById(int registrationId) {
        Registrations registration = eventRepository.findRegistrationById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("Registration not found");
        }
        return registration;
    }
    
    @Override
    @Transactional
    public Registrations confirmRegistration(int registrationId, int eventId) {
        Registrations registration = findRegistrationById(registrationId);
            
        if (registration.getEvent().getEventId() != eventId) {
            throw new IllegalArgumentException("Registration does not belong to the specified event");
        }
        
        if (registration.getStatus() == Registrations.RegistrationStatus.CONFIRMED) {
            return registration; // Already confirmed
        }
        
        // Check seat availability
        EventSeating seating = eventSeatingRepository.findByEventId(eventId);
        if (seating != null && seating.getAvailableSeat() <= 0) {
            throw new IllegalStateException("No seats available for confirmation");
        }
        
        // Update status
        registration.setStatus(Registrations.RegistrationStatus.CONFIRMED);
        
        // Update seat count if needed
        if (seating != null) {
            seating.setSeatsBooked(seating.getSeatsBooked() + 1);
            eventSeatingRepository.save(seating);
        }
        
        // Save the user to cascade the registration update
        registration.getStudent().getRegistrations().add(registration);
        userRepository.save(registration.getStudent());
        
        return registration;
    }
    
    @Override
    @Transactional
    public Registrations cancelRegistration(int registrationId, int eventId) {
        Registrations registration = findRegistrationById(registrationId);
            
        if (registration.getEvent().getEventId() != eventId) {
            throw new IllegalArgumentException("Registration does not belong to the specified event");
        }
        
        if (registration.getStatus() == Registrations.RegistrationStatus.CANCELLED) {
            return registration; // Already cancelled
        }
        
        // Update seat count if it was confirmed
        if (registration.getStatus() == Registrations.RegistrationStatus.CONFIRMED) {
            EventSeating seating = eventSeatingRepository.findByEventId(eventId);
            if (seating != null && seating.getSeatsBooked() > 0) {
                seating.setSeatsBooked(seating.getSeatsBooked() - 1);
                eventSeatingRepository.save(seating);
            }
        }
        
        // Update status
        registration.setStatus(Registrations.RegistrationStatus.CANCELLED);
        
        // Save the user to cascade the registration update
        registration.getStudent().getRegistrations().add(registration);
        userRepository.save(registration.getStudent());
        
        return registration;
    }
    
    @Override
    @Transactional
    public Registrations updateRegistrationStatus(int registrationId, int eventId, String status) {
        Registrations registration = findRegistrationById(registrationId);
            
        if (registration.getEvent().getEventId() != eventId) {
            throw new IllegalArgumentException("Registration does not belong to the specified event");
        }
        
        Registrations.RegistrationStatus newStatus = Registrations.RegistrationStatus.valueOf(status.toUpperCase());
        Registrations.RegistrationStatus oldStatus = registration.getStatus();
        
        if (oldStatus == newStatus) {
            return registration; // No change needed
        }
        
        // Handle seat count changes
        EventSeating seating = eventSeatingRepository.findByEventId(eventId);
        if (seating != null) {
            // If changing to CONFIRMED from another status
            if (newStatus == Registrations.RegistrationStatus.CONFIRMED && 
                oldStatus != Registrations.RegistrationStatus.CONFIRMED) {
                if (seating.getAvailableSeat() <= 0) {
                    throw new IllegalStateException("No seats available for confirmation");
                }
                seating.setSeatsBooked(seating.getSeatsBooked() + 1);
                eventSeatingRepository.save(seating);
            } 
            // If changing from CONFIRMED to another status
            else if (oldStatus == Registrations.RegistrationStatus.CONFIRMED && 
                    newStatus != Registrations.RegistrationStatus.CONFIRMED) {
                seating.setSeatsBooked(Math.max(0, seating.getSeatsBooked() - 1));
                eventSeatingRepository.save(seating);
            }
        }
        
        // Update the status
        registration.setStatus(newStatus);
        
        // Save the user to cascade the registration update
        registration.getStudent().getRegistrations().add(registration);
        userRepository.save(registration.getStudent());
        
        return registration;
    }

    public byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
