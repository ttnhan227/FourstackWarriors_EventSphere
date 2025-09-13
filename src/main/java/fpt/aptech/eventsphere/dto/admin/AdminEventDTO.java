package fpt.aptech.eventsphere.dto.admin;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.admin.EventsModel;
import lombok.*;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEventDTO {
    private Integer eventId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private Events.EventStatus eventStatus;

    private Integer venueId;
    private String venueName;
    private String venueAddress;

    private Integer organizerId;
    private String organizerName;
    private String organizerEmail;
    private String organizerPhone;
    private String organizerDeparment;

//    public String getOrganizerDeparment() {
//        return organizerDeparment;
//    }
//
//    public void setOrganizerDeparment(String organizerDeparment) {
//        this.organizerDeparment = organizerDeparment;
//    }

    private Integer totalSeats;
    private Integer seatsBooked;
    private Integer availableSeats;
    private Boolean waitlistEnabled;
//register
    private Long totalRegistrations;
    private Long confirmedRegistrations;
    private Long cancelledRegistrations;
    private Long waitlistRegistrations;
//event moderation
    private Integer eventModelId;
    private EventsModel.Status moderationStatus;
    private LocalDateTime submitAt;
    private LocalDateTime updatedAt;
    private String reviewedByName;
    private String adminComment;
    private String organizerComment;

    private Long totalAttendance;
    private Long actualAttendees;

    private Long totalFeedbacks;
    private Double averageRating;

    private Long totalMediaFiles;

    private Long totalCertificatesIssued;

    private LocalDateTime eventCreatedAt;
    private LocalDateTime eventUpdatedAt;

    public Integer getAvailableSeats() {
        if (totalSeats != null && seatsBooked != null) {
            return totalSeats - seatsBooked;
        }
        return 0;
    }

    public Double getBookingPercentage() {
        if (totalSeats != null && totalSeats > 0 && seatsBooked != null) {
            return (seatsBooked.doubleValue() / totalSeats.doubleValue()) * 100;
        }
        return 0.0;
    }

    public Double getAttendanceRate() {
        if (confirmedRegistrations != null && confirmedRegistrations > 0 && actualAttendees != null) {
            return (actualAttendees.doubleValue() / confirmedRegistrations.doubleValue()) * 100;
        }
        return 0.0;
    }

    public String getEventStatusDisplay() {
        if (eventStatus == null) return "Pending";
        switch (eventStatus) {
            case PENDING:
                return "Pending";
            case APPROVED:
                return "Approved";
            case REJECTED:
                return "Rejected";
            case COMPLETED:
                return "Completed";
            default:
                return eventStatus.toString();
        }
    }

    public String getEventStatusColor() {
        if (eventStatus == null) return "secondary";
        switch (eventStatus) {
            case PENDING:
                return "warning";
            case APPROVED:
                return "success";
            case REJECTED:
                return "danger";
            case COMPLETED:
            default:
                return "secondary";
        }
    }

    public boolean isOngoing() {
        if (startDate == null || endDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isCancelled() {
        return eventStatus == Events.EventStatus.CANCELLED;
    }

}
