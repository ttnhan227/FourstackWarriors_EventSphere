package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.repositories.admin.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class AdminEventManagementServiceImpl implements AdminEventManagementService{

    private final AdminEventManagRepo adminEventManagRepo;
    private final AdminEventModelRepository adminEventModelRepository;
    @Override
    @Transactional(readOnly = true)
    public List<AdminEventDTO> findAllEvents() {
        log.info("Fetching all events for admin management");
        List<Events> events = adminEventManagRepo.findAllEventsWithDetails();
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminEventDTO> searchAndFilterEvents(AdminEventSearchDTO searchDTO) {
        log.info("Searching events with criteria: {}", searchDTO.getKeyword());

        Sort sort = createSort(searchDTO.getSortBy(), searchDTO.getSortDirection());
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<Events> eventsPage = adminEventManagRepo.searchEvents(
                searchDTO.hasKeyword() ? searchDTO.getKeyword() : null,
                searchDTO.hasCategory() ? searchDTO.getCategory() : null,
                searchDTO.hasStatus() ? searchDTO.getStatus() : null,
                searchDTO.hasOrganizer() ? searchDTO.getOrganizerName() : null,
                searchDTO.getOrganizerEmail(),
                searchDTO.hasVenue() ? searchDTO.getVenueName() : null,
                searchDTO.getStartDateFrom(),
                searchDTO.getStartDateTo(),
                searchDTO.getEndDateFrom(),
                searchDTO.getEndDateTo(),
                pageable
        );

        return eventsPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminEventDTO> getEventDetails(Integer eventId) {
        log.info("Fetching event details for ID: {}", eventId);
        return adminEventManagRepo.findEventWithAllDetails(eventId)
                .map(this::convertToDetailDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalEvents() {
        return adminEventManagRepo.countTotalEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getOngoingEvents() {
        return adminEventManagRepo.countOngoingEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCancelledEvents() {
        return adminEventManagRepo.countCancelledEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getEventStatusStatistics() {
        List<Object[]> stats = adminEventManagRepo.getEventStatusStatistics();
        Map<String, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            Events.EventStatus status = (Events.EventStatus) stat[0];
            Long count = (Long) stat[1];
            result.put(status != null ? status.toString() : "UNKNOWN", count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getEventCategoryStatistics() {
        List<Object[]> stats = adminEventManagRepo.getEventCategoryStatistics();
        Map<String, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            String category = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(category != null ? category : "UNCATEGORIZED", count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getEventsByMonth(int months) {
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> stats = adminEventManagRepo.getEventsByMonth(fromDate);
        Map<String, Long> result = new LinkedHashMap<>();

        for (Object[] stat : stats) {
            Integer year = (Integer) stat[0];
            Integer month = (Integer) stat[1];
            Long count = (Long) stat[2];
            String monthYear = String.format("%02d/%d", month, year);
            result.put(monthYear, count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return adminEventManagRepo.findAllCategories();
    }

    @Override
    public boolean deleteEvent(Integer eventId) {
        try {
            Optional<Events> eventOpt = adminEventManagRepo.findById(eventId);
            if (eventOpt.isPresent()) {
                Events event = eventOpt.get();
                event.setStatus(Events.EventStatus.CANCELLED);
                adminEventManagRepo.save(event);
                log.info("Event {} has been cancelled", eventId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting event {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean restoreEvent(Integer eventId) {
        try {
            Optional<Events> eventOpt = adminEventManagRepo.findById(eventId);
            if (eventOpt.isPresent()) {
                Events event = eventOpt.get();
                Events.EventStatus newStatus = calculateAppropriateStatus(event);
                event.setStatus(newStatus);
                adminEventManagRepo.save(event);
                log.info("Event {} has been restored to status {}", eventId, newStatus);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error restoring event {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateEventStatus(Integer eventId, Events.EventStatus status) {
        try {
            Optional<Events> eventOpt = adminEventManagRepo.findById(eventId);
            if (eventOpt.isPresent()) {
                Events event = eventOpt.get();
                event.setStatus(status);
                adminEventManagRepo.save(event);
                log.info("Event {} status updated to {}", eventId, status);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error updating event {} status: {}", eventId, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminEventDTO> getEventsForExport(AdminEventSearchDTO searchDTO) {
        log.info("Exporting events with criteria");
        searchDTO.setPage(0);
        searchDTO.setSize(Integer.MAX_VALUE);
        Page<AdminEventDTO> result = searchAndFilterEvents(searchDTO);
        return result.getContent();
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return switch (sortBy.toLowerCase()) {
            case "title" -> Sort.by(direction, "title");
            case "category" -> Sort.by(direction, "category");
            case "startdate" -> Sort.by(direction, "startDate");
            case "enddate" -> Sort.by(direction, "endDate");
            case "status" -> Sort.by(direction, "status");
            case "organizer" -> Sort.by(direction, "organizer.userDetails.fullName");
            default -> Sort.by(direction, "startDate");
        };
    }

    private Events.EventStatus calculateAppropriateStatus(Events event) {
        LocalDateTime now = LocalDateTime.now();

        if (event.getEndDate().isBefore(now)) {
            return Events.EventStatus.COMPLETED;
        } else if (event.getStartDate().isBefore(now) && event.getEndDate().isAfter(now)) {
            return Events.EventStatus.ONGOING;
        } else {
            return Events.EventStatus.PUBLISHED;
        }
    }

    private AdminEventDTO convertToDTO(Events event) {
        AdminEventDTO dto = AdminEventDTO.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .imageUrl(event.getImageUrl())
                .eventStatus(event.getStatus())
                .eventCreatedAt(LocalDateTime.now())
                .build();

        if (event.getVenue() != null) {
            dto.setVenueId(event.getVenue().getVenueId());
            dto.setVenueName(event.getVenue().getName());
            dto.setVenueAddress(event.getVenue().getAddress());
        }

        if (event.getOrganizer() != null) {
            dto.setOrganizerId(event.getOrganizer().getUserId());
            dto.setOrganizerEmail(event.getOrganizer().getEmail());

            if (event.getOrganizer().getUserDetails() != null) {
                UserDetails ud = event.getOrganizer().getUserDetails();
                dto.setOrganizerName(ud.getFullName());
                dto.setOrganizerPhone(ud.getPhone());
                dto.setOrganizerDeparment(ud.getDepartment());
            }
        }

        // event seat
        if (event.getEventSeating() != null) {
            EventSeating seating = event.getEventSeating();
            dto.setTotalSeats(seating.getTotalSeats());
            dto.setSeatsBooked(seating.getSeatsBooked());
            dto.setWaitlistEnabled(seating.isWaitlistEnabled());
        }

        return dto;
    }

    private AdminEventDTO convertToDetailDTO(Events event) {
        AdminEventDTO dto = convertToDTO(event);

        // thống kê regist
        List<Registrations> registrations = event.getRegistrations();
        if (registrations != null) {
            dto.setTotalRegistrations((long) registrations.size());
            dto.setConfirmedRegistrations(
                    registrations.stream()
                            .filter(r -> r.getStatus() == Registrations.RegistrationStatus.CONFIRMED)
                            .count()
            );
            dto.setCancelledRegistrations(
                    registrations.stream()
                            .filter(r -> r.getStatus() == Registrations.RegistrationStatus.CANCELLED)
                            .count()
            );
            dto.setWaitlistRegistrations(
                    registrations.stream()
                            .filter(r -> r.getStatus() == Registrations.RegistrationStatus.WAITLIST)
                            .count()
            );
        }

        // thống kê attendance
        List<Attendance> attendances = event.getAttendances();
        if (attendances != null) {
            dto.setTotalAttendance((long) attendances.size());
            dto.setActualAttendees(
                    attendances.stream()
                            .filter(Attendance::isAttended)
                            .count()
            );
        }

        // tthống kê feedback
        List<Feedback> feedbacks = event.getFeedbacks();
        if (feedbacks != null) {
            dto.setTotalFeedbacks((long) feedbacks.size());
            dto.setAverageRating(
                    feedbacks.stream()
                            .mapToDouble(Feedback::getRating)
                            .average()
                            .orElse(0.0)
            );
        }

        //moderation
        adminEventModelRepository.findByEventEventId(event.getEventId())
                .ifPresent(eventModel -> {
                    dto.setEventModelId(eventModel.getEventModelId());
                    dto.setModerationStatus(eventModel.getStatus());
                    dto.setSubmitAt(eventModel.getSubmitAt());
                    dto.setUpdatedAt(eventModel.getUpdatedAt());
                    dto.setAdminComment(eventModel.getAdminComment());
                    dto.setOrganizerComment(eventModel.getOrgannizerComment());

                    if (eventModel.getReviewBy() != null) {
                        dto.setReviewedByName(eventModel.getReviewBy().getEmail());
                    }
                });

        return dto;
    }
}

