package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.models.admin.EventsModel;
import fpt.aptech.eventsphere.models.admin.EventsModel.Status;
import fpt.aptech.eventsphere.repositories.UserRepository;
import fpt.aptech.eventsphere.repositories.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventModerationService {

    @Autowired
    private AdminEventModelRepository adminEventModelRepository;

    @Autowired
    private AdminEventRepository adminEventRepository;

    @Autowired
    private UserRepository userRepository;

public Page<EventModerationDTO> searchAndSortAllEvents(EventModelSearchDTO searchRequest) {
    System.out.println("=== DEBUG EventModerationService.searchAndSortAllEvents ===");
    System.out.println("Search request status: " + searchRequest.getStatus());
    System.out.println("Keyword: " + searchRequest.getKeyword());
    System.out.println("Category: " + searchRequest.getCategory());
    System.out.println("Organizer: " + searchRequest.getOrganizerName());

    List<Object[]> results = adminEventModelRepository.searchEventsModel(
        searchRequest.getStatus(), // This can be null to get all statuses
        searchRequest.hasKeyword() ? searchRequest.getKeyword() : null,
        searchRequest.hasCategory() ? searchRequest.getCategory() : null,
        searchRequest.hasOrganizer() ? searchRequest.getOrganizerName() : null
    );

    System.out.println("Raw results from database: " + results.size());

    // Convert to DTOs
    List<EventModerationDTO> events = convertToEventModerationDTO(results);
    System.out.println("Converted to DTOs: " + events.size());

    // Apply sorting
    events = applySorting(events, searchRequest.getSortBy(), searchRequest.getSortDirection());
    System.out.println("After sorting: " + events.size());

    // Apply pagination
    int start = searchRequest.getPage() * searchRequest.getSize();
    int end = Math.min(start + searchRequest.getSize(), events.size());
    
    System.out.println("Pagination - start: " + start + ", end: " + end);
    
    List<EventModerationDTO> pagedEvents = events.subList(start, end);
    System.out.println("Paged events: " + pagedEvents.size());
    
    return new PageImpl<>(pagedEvents, 
                        PageRequest.of(searchRequest.getPage(), searchRequest.getSize()), 
                        events.size());
}

    private List<EventModerationDTO> convertToEventModerationDTO(List<Object[]> results) {
        return results.stream().map(row -> {
            EventModerationDTO dto = new EventModerationDTO();
            
            dto.setEventModelId((Integer) row[0]);
            dto.setStatus((Status) row[1]);
            dto.setSubmitAt((LocalDateTime) row[2]);
            dto.setUpdatedAt((LocalDateTime) row[3]);
            dto.setAdminComment((String) row[4]);
            dto.setOrgannizerComment((String) row[5]);
            
            dto.setEventId((Integer) row[6]);
            dto.setTitle((String) row[7]);
            dto.setDescription((String) row[8]);
            dto.setCategory((String) row[9]);
            dto.setStartDate((LocalDateTime) row[10]);
            dto.setEndDate((LocalDateTime) row[11]);
            dto.setImageUrl((String) row[12]);
            
            dto.setVenueName((String) row[13]);
            dto.setOrganizerId((Integer) row[14]);
            dto.setOrganizerName((String) row[15]);
            dto.setOrganizerEmail((String) row[16]);
            
            dto.setTotalSeats((Integer) row[17]);
            dto.setSeatsBooked((Integer) row[18]);
            dto.setWaitlistEnabled((Boolean) row[19]);

//            dto.setReviewedByEmail((String) row[20]);
            dto.setModerationCreatedAt((LocalDateTime) row[21]);
            
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean submitEventForReview(Integer eventId, String organizerComment) {
        try {
            Events event = adminEventRepository.findById(eventId).orElse(null);
            if (event == null) return false;

            Optional<EventsModel> existing = adminEventModelRepository.findByEventEventId
                    (eventId);
            if (existing.isPresent()) {
                EventsModel eventsModel = existing.get();
                eventsModel.setStatus(Status.PENDING);
                eventsModel.setSubmitAt(LocalDateTime.now());
                eventsModel.setOrgannizerComment(organizerComment);
                adminEventModelRepository.save(eventsModel);
            } else {
                EventsModel eventsModel = new EventsModel();
                eventsModel.setEvent(event);
                eventsModel.setStatus(Status.PENDING);
                eventsModel.setSubmitAt(LocalDateTime.now());
                eventsModel.setOrgannizerComment(organizerComment);
                adminEventModelRepository.save(eventsModel);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean approveEvent(Integer eventModelId, String adminComment, String adminEmail) {
        try {
            EventsModel eventsModel = adminEventModelRepository.findById(eventModelId).orElse(null);
            if (eventsModel == null) return false;

            Users admin = userRepository.findByEmail(adminEmail).orElse(null);
            if (admin == null) return false;

            eventsModel.setStatus(Status.APPROVED);
            eventsModel.setReviewBy(admin);
            eventsModel.setAdminComment(adminComment);

            adminEventModelRepository.save(eventsModel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean rejectEvent(Integer eventModelId, String adminComment, String adminEmail) {
        try {
            EventsModel eventsModel = adminEventModelRepository.findById(eventModelId).orElse(null);
            if (eventsModel == null) return false;

            Users admin = userRepository.findByEmail(adminEmail).orElse(null);
            if (admin == null) return false;

            eventsModel.setStatus(Status.REJECTED);
            eventsModel.setReviewBy(admin);
            eventsModel.setAdminComment(adminComment);

            adminEventModelRepository.save(eventsModel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean requestEventChange(Integer eventModelId, String adminComment, String adminEmail) {
        try {
            EventsModel eventsModel = adminEventModelRepository.findById(eventModelId).orElse(null);
            if (eventsModel == null) return false;

            Users admin = userRepository.findByEmail(adminEmail).orElse(null);
            if (admin == null) return false;

            eventsModel.setStatus(Status.CHANGE_REQUESTED);
            eventsModel.setReviewBy(admin);
            eventsModel.setAdminComment(adminComment);

            adminEventModelRepository.save(eventsModel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }





    public long getEventCountByStatus(Status status) {
        return adminEventModelRepository.countByStatus(status);
    }

    public Optional<EventModerationDTO> getEventModerationById(Integer eventModelId) {
        return adminEventModelRepository.findById(eventModelId)
            .map(this::convertToDTO);
    }

    private EventModerationDTO convertToDTO(EventsModel eventsModel) {
        EventModerationDTO dto = new EventModerationDTO();
        Events event = eventsModel.getEvent();
        
        // EventsModel info
        dto.setEventModelId(eventsModel.getEventModelId());
        dto.setStatus(eventsModel.getStatus());
        dto.setSubmitAt(eventsModel.getSubmitAt());
        dto.setUpdatedAt(eventsModel.getUpdatedAt());
        dto.setAdminComment(eventsModel.getAdminComment());
        dto.setOrgannizerComment(eventsModel.getOrgannizerComment());
        dto.setModerationCreatedAt(eventsModel.getCreatedAt());
        
        // Event info
        dto.setEventId(event.getEventId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setCategory(event.getCategory());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setImageUrl(event.getImageUrl());
        
        // Venue and organizer
        if (event.getVenue() != null) {
            dto.setVenueName(event.getVenue().getName());
        }
        
        if (event.getOrganizer() != null) {
            dto.setOrganizerId(event.getOrganizer().getUserId());
            dto.setOrganizerEmail(event.getOrganizer().getEmail());
            if (event.getOrganizer().getUserDetails() != null) {
                dto.setOrganizerName(event.getOrganizer().getUserDetails().getFullName());
            } else {
                dto.setOrganizerName(event.getOrganizer().getEmail());
            }
        }
        
        // Seating info
        if (event.getEventSeating() != null) {
            dto.setTotalSeats(event.getEventSeating().getTotalSeats());
            dto.setSeatsBooked(event.getEventSeating().getSeatsBooked());
            dto.setWaitlistEnabled(event.getEventSeating().isWaitlistEnabled());
        }
        
        // Reviewer info
        if (eventsModel.getReviewBy() != null) {
            dto.setReviewedByName(eventsModel.getReviewBy().getEmail());
        }
        
        return dto;
    }

    private List<EventModerationDTO> applySorting(List<EventModerationDTO> events, String sortBy, String sortDirection) {
        Comparator<EventModerationDTO> comparator;

        switch (sortBy.toLowerCase()) {
            case "title":
                comparator = Comparator.comparing(e -> e.getTitle() != null ? e.getTitle().toLowerCase() : "");
                break;
            case "category":
                comparator = Comparator.comparing(e -> e.getCategory() != null ? e.getCategory().toLowerCase() : "");
                break;
            case "organizername":
                comparator = Comparator.comparing(e -> e.getOrganizerName() != null ? e.getOrganizerName().toLowerCase() : "");
                break;
            case "startdate":
                comparator = Comparator.comparing(e -> e.getStartDate() != null ? e.getStartDate() : LocalDateTime.MIN);
                break;
            case "status":
                comparator = Comparator.comparing(EventModerationDTO::getStatus);
                break;
            case "updatedat":
                comparator = Comparator.comparing(e -> e.getUpdatedAt() != null ? e.getUpdatedAt() : LocalDateTime.MIN);
                break;
            default: // submitAt
                comparator = Comparator.comparing(e -> e.getSubmitAt() != null ? e.getSubmitAt() : LocalDateTime.MIN);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return events.stream()
                   .sorted(comparator)
                   .collect(Collectors.toList());
    }

}