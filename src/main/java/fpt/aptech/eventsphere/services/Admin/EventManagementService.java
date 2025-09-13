package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.EventManagementDTO;
import fpt.aptech.eventsphere.dto.admin.EventSearchRequestDTO;
import fpt.aptech.eventsphere.dto.admin.EventWithCountDTO;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventManagementService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventManagementService.class);
    @Autowired
    private AdminEventRepository adminEventRepository;

    @Transactional(readOnly = true)
    public EventManagementDTO getEventById(Integer id) {
        log.debug("Fetching event by ID: {}", id);
        if (id == null) {
            log.warn("Attempted to fetch event with null ID");
            return null;
        }

        try {
            // Get the event with registrations, organizer, and venue in a single query
            EventWithCountDTO eventWithCount = adminEventRepository.findEventWithRegistrations(id.intValue())
                    .orElseThrow(() -> {
                        log.warn("Event with ID {} not found", id);
                        return new RuntimeException("Event not found with id: " + id);
                    });
            
            Events event = eventWithCount.getEvent();
            log.debug("Found event: ID={}, Title={}", event.getEventId(), event.getTitle());

            // Log registration details for debugging
            if (event.getRegistrations() != null) {
                int size = event.getRegistrations().size();
                log.debug("Event has {} registrations, {} confirmed", size, eventWithCount.getConfirmedCount());
                if (size > 0) {
                    event.getRegistrations().forEach(reg ->
                            log.debug("Registration ID: {}, Status: {}",
                                    reg.getRegistrationId(),
                                    reg.getStatus() != null ? reg.getStatus() : "NULL"));

                    // Log the number of confirmed registrations
                    long confirmedCount = event.getRegistrations().stream()
                            .filter(reg -> reg.getStatus() == Registrations.RegistrationStatus.CONFIRMED)
                            .count();
                    log.debug("Event has {} confirmed registrations", confirmedCount);
                }
            } else {
                log.debug("Event has no registrations");
            }

            // Convert to DTO with confirmed count from the query
            return convertToDto(event, eventWithCount.getConfirmedCount());
        } catch (Exception e) {
            log.error("Error fetching event with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Page<EventManagementDTO> searchAndSortEvents(EventSearchRequestDTO searchRequest) {
        try {
            // Log search parameters
            log.debug("Searching events with params - keyword: '{}', category: '{}', organizerName: '{}', status: '{}', sortBy: '{}', sortDirection: '{}', page: {}, size: {}",
                    searchRequest.hasKeyword() ? searchRequest.getKeyword() : "<none>",
                    searchRequest.hasCategory() ? searchRequest.getCategory() : "<all>",
                    searchRequest.hasOrganizerName() ? searchRequest.getOrganizerName() : "<any>",
                    searchRequest.hasStatus() ? searchRequest.getStatus() : "<any>",
                    searchRequest.getSortBy(),
                    searchRequest.getSortDirection(),
                    searchRequest.getPage(),
                    searchRequest.getSize());

            // Create pageable with sorting
            Sort sort = searchRequest.getSort();
            Pageable pageable = PageRequest.of(
                    searchRequest.getPage(),
                    searchRequest.getSize(),
                    sort
            );

            // Get normalized search parameters
            String keyword = searchRequest.hasKeyword() ? searchRequest.getKeyword().trim().toLowerCase() : null;
            String category = searchRequest.hasCategory() ? searchRequest.getCategory().trim() : null;
            String organizerName = searchRequest.hasOrganizerName() ? searchRequest.getOrganizerName().trim().toLowerCase() : null;

            log.debug("Searching events with normalized params - keyword: '{}', category: '{}', organizerName: '{}', status: {}",
                    keyword, category, organizerName, searchRequest.getStatus());

            // Get status from search request
            Events.EventStatus status = null;
            if (searchRequest.hasStatus()) {
                try {
                    status = Events.EventStatus.valueOf(searchRequest.getStatus().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status value: {}", searchRequest.getStatus());
                }
            }

            // Use the repository method with logging
            Page<EventWithCountDTO> eventsPage = adminEventRepository.searchEventsWithLogging(
                    keyword, category, organizerName, status, pageable
            );

            if (eventsPage.getTotalElements() == 0) {
                log.warn("No events found matching the search criteria");
            }

            // Convert Page<EventWithCountDTO> to Page<EventManagementDTO>
            return eventsPage.map(dto -> convertToDto(dto.getEvent(), dto.getConfirmedCount()));

        } catch (Exception e) {
            log.error("Error in searchAndSortEvents: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converts an Events entity to EventManagementDTO with confirmed count
     */
    private EventManagementDTO convertToDto(Events event, long confirmedCount) {
        if (event == null) {
            log.warn("Attempted to convert null event to DTO");
            return null;
        }

        try {
            EventManagementDTO dto = new EventManagementDTO();

            // Basic fields
            dto.setId((long) event.getEventId());
            dto.setName(event.getTitle());
            dto.setDescription(event.getDescription());
            dto.setStartDate(event.getStartDate());
            dto.setEndDate(event.getEndDate());
            dto.setConfirmedRegistrations(confirmedCount);

            // Related entities
            String location = "";
            if (event.getVenue() != null) {
                location = event.getVenue().getName();
                log.debug("Event ID: {} - Venue: {}", event.getEventId(), location);
            } else {
                log.warn("Event ID: {} has no venue", event.getEventId());
            }
            dto.setLocation(location);

            String organizerName = "";
            String organizerEmail = "";
            String organizerPhone = "";
            String organizerDepartment = "";

            if (event.getOrganizer() != null) {
                // Get basic organizer info from Users entity
                organizerEmail = event.getOrganizer().getEmail();

                // Get additional info from UserDetails if available
                try {
                    // Since UserDetails is LAZY loaded, we need to check if it's initialized
                    if (event.getOrganizer().getUserDetails() != null) {
                        fpt.aptech.eventsphere.models.UserDetails userDetails = event.getOrganizer().getUserDetails();
                        if (userDetails != null) {
                            organizerName = userDetails.getFullName() != null ? userDetails.getFullName() : "";
                            organizerPhone = userDetails.getPhone() != null ? userDetails.getPhone() : "";
                            organizerDepartment = userDetails.getDepartment() != null ? userDetails.getDepartment() : "";
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error fetching user details for organizer: {}", e.getMessage());
                }

                // If no name was found, use email as fallback
                if ((organizerName == null || organizerName.isEmpty()) && organizerEmail != null) {
                    organizerName = organizerEmail;
                }

                log.debug("Event ID: {} - Organizer: {} (Email: {})", event.getEventId(), organizerName, organizerEmail);
            } else {
                log.warn("Event ID: {} has no organizer", event.getEventId());
            }
            dto.setOrganizerName(organizerName);
            dto.setOrganizerEmail(organizerEmail);
            dto.setOrganizerPhone(organizerPhone);
            dto.setOrganizerDepartment(organizerDepartment);

            // Category and image
            dto.setCategory(event.getCategory());
            dto.setImageUrl(event.getImageUrl());

            // Count confirmed participants
            int confirmedParticipants = 0;
            if (event.getRegistrations() != null && !event.getRegistrations().isEmpty()) {
                log.debug("Processing {} registrations for event ID {}", event.getRegistrations().size(), event.getEventId());

                confirmedParticipants = (int) event.getRegistrations().stream()
                        .peek(reg -> log.debug("Registration ID: {}, Status: {}",
                                reg.getRegistrationId(),
                                reg.getStatus() != null ? reg.getStatus() : "NULL"))
                        .filter(reg -> reg.getStatus() == Registrations.RegistrationStatus.CONFIRMED)
                        .peek(reg -> log.debug("Confirmed registration found: {}", reg.getRegistrationId()))
                        .count();

                log.debug("Event ID {} has {} confirmed participants", event.getEventId(), confirmedParticipants);
            } else {
                log.debug("Event ID {} has no registrations or registrations collection is null", event.getEventId());
            }
            dto.setCurrentParticipants(confirmedParticipants);
            dto.setActive(true);
            log.debug("Set current participants to: {}", confirmedParticipants);

            // Set status
            if (event.getStatus() != null) {
                dto.setStatus(event.getStatus().name());
            } else {
                dto.setStatus(Events.EventStatus.PENDING.name());
                log.warn("Event ID: {} has no status, defaulting to PENDING", event.getEventId());
            }

            log.debug("Successfully converted event ID {} to DTO", event.getEventId());
            return dto;
        } catch (Exception e) {
            log.error("Error converting event ID {} to DTO: {}", event != null ? event.getEventId() : "null", e.getMessage(), e);
            throw new RuntimeException("Error converting event to DTO", e);
        }
    }


    @Transactional
    public boolean updateEventDetails(int eventId, String title, String description, String category,
                                      LocalDateTime startDate, LocalDateTime endDate, String imagePath) {
        try {
            log.debug("Updating event details for event ID: {}, new image path: {}", eventId, imagePath);
            int updated = adminEventRepository.updateEventDetails(
                    eventId,
                    title,
                    description,
                    category,
                    startDate,
                    endDate,
                    imagePath // Store only the filename in the database
            );
            
            if (updated > 0) {
                log.debug("Successfully updated event ID: {}", eventId);
                return true;
            } else {
                log.warn("No records were updated for event ID: {}", eventId);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getAllEventCategories() {
        return adminEventRepository.findAllEventCategories();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getEventStatistics() {
        Map<String, Long> stats = new HashMap<>();

        // Get total events
        long totalEvents = adminEventRepository.count();

        // Get pending events
        long pendingEvents = adminEventRepository.countByStatus(Events.EventStatus.PENDING);

        // Get approved events
        long approvedEvents = adminEventRepository.countByStatus(Events.EventStatus.APPROVED);

        // Get rejected events
        long rejectedEvents = adminEventRepository.countByStatus(Events.EventStatus.REJECTED);

        stats.put("totalEvents", totalEvents);
        stats.put("pendingEvents", pendingEvents);
        stats.put("approvedEvents", approvedEvents);
        stats.put("rejectedEvents", rejectedEvents);

        return stats;
    }
}
