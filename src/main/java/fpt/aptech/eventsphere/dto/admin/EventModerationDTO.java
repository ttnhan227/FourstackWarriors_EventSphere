package fpt.aptech.eventsphere.dto.admin;

import fpt.aptech.eventsphere.models.admin.EventsModel.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventModerationDTO {
    private Integer eventModelId;
    private Integer eventId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private String venueName;

    private Integer organizerId;
    private String organizerName;
    private String organizerEmail;

    private Integer totalSeats;
    private Integer seatsBooked;
    private Boolean waitlistEnabled;

    private Status status;
    private LocalDateTime submitAt;
    private LocalDateTime updatedAt;
    private String reviewedByName;
    private String adminComment;
    private String organnizerComment;

    private LocalDateTime eventCreatedAt;
    private LocalDateTime moderationCreatedAt;

    private String moderationAction; // approve, reject, request_change
    private String moderationComments;

    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        switch (status) {
            case PENDING: return "Pending Review";
            case APPROVED: return "Approved";
            case REJECTED: return "Rejected";
            case CHANGE_REQUESTED: return "Changes Requested";
            case CANCELLED: return "Cancelled";
            case FINISHED: return "Finished";
            default: return status.toString();
        }
    }
    
    public String getStatusColor() {
        if (status == null) return "secondary";
        switch (status) {
            case PENDING: return "warning";
            case APPROVED: return "success";
            case REJECTED: return "danger";
            case CHANGE_REQUESTED: return "info";
            case CANCELLED: return "dark";
            case FINISHED: return "primary";
            default: return "secondary";
        }
    }

    public boolean canBeApproved() {
        return status == Status.PENDING || status == Status.CHANGE_REQUESTED;
    }

    public boolean canBeRejected() {
        return status == Status.PENDING || status == Status.CHANGE_REQUESTED;
    }

    public boolean canRequestChange() {
        return status == Status.PENDING;
    }
}