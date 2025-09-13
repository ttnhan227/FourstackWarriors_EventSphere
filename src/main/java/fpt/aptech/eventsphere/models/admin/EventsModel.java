package fpt.aptech.eventsphere.models.admin;

import fpt.aptech.eventsphere.models.*;
import jakarta.persistence.*;
import jdk.jfr.Event;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events_Model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventsModel {
    
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED,
        CHANGE_REQUESTED,
        FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_model_id")
    private Integer eventModelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Events event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "submit_at")
    private LocalDateTime submitAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_by")
    private Users reviewBy;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "organnizer_comment", columnDefinition = "TEXT")
    private String organnizerComment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (submitAt == null) {
            submitAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Utility methods
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

    public boolean isUnderReview() {
        return status == Status.PENDING || status == Status.CHANGE_REQUESTED;
    }

    public boolean isFinalStatus() {
        return status == Status.APPROVED || status == Status.REJECTED || 
               status == Status.CANCELLED || status == Status.FINISHED;
    }

    // Convert to Events.EventStatus
    public Events.EventStatus toEventStatus() {
        switch (status) {
            case PENDING:
                return Events.EventStatus.PENDING;
            case APPROVED:
                return Events.EventStatus.APPROVED;
            case REJECTED:
                return Events.EventStatus.REJECTED;
            case CANCELLED:
                return Events.EventStatus.CANCELLED;
            case FINISHED:
                return Events.EventStatus.COMPLETED;
            case CHANGE_REQUESTED:
                return Events.EventStatus.CHANGE_REQUESTED;
            default:
                return Events.EventStatus.DRAFT;
        }
    }

    // Create from Events.EventStatus
    public static Status fromEventStatus(Events.EventStatus eventStatus) {
        switch (eventStatus) {
            case PENDING:
                return Status.PENDING;
            case APPROVED:
                return Status.APPROVED;
            case REJECTED:
                return Status.REJECTED;
            case CANCELLED:
                return Status.CANCELLED;
            case COMPLETED:
                return Status.FINISHED;
            case CHANGE_REQUESTED:
                return Status.CHANGE_REQUESTED;
            default:
                return Status.PENDING;
        }
    }
}
