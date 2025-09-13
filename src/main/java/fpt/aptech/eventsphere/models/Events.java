package fpt.aptech.eventsphere.models;

import fpt.aptech.eventsphere.validations.ValidDateRange;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange
public class Events {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int eventId;
    @NotBlank(message = "Title must not be empty")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(name = "title", length = 150, nullable = false)
    private String title;
    @NotBlank(message = "Description must not be empty")
    @Size(max = 1024, message = "Description must not exceed 1024 characters")
    @Column(name = "description")
    private String description;
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(name = "category", length = 50)
    private String category;
    @Column(name = "startDate")
    @NotNull(message = "please enter start date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @Column(name = "endDate")
    @NotNull(message = "please enter end date")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    @Column(name = "image_url", length = 512)
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status = EventStatus.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venues venue;
    //to send email before event happen
    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;
    // Many-to-one with Users (organizer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Users organizer;
    // One-to-many relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registrations> registrations = new ArrayList<>();
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();
    //one to one
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private EventSeating eventSeating;

    //set eventseating and set event
    public void setEventSeating(EventSeating eventSeating) {
        this.eventSeating = eventSeating;
        if (eventSeating != null) {
            eventSeating.setEvent(this);
        }
    }

    // Utility methods for status checking
    public boolean isDraft() {
        return status == EventStatus.DRAFT;
    }

    public boolean isPending() {
        return status == EventStatus.PENDING;
    }

    public boolean isApproved() {
        return status == EventStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == EventStatus.REJECTED;
    }

    public boolean isPublished() {
        return status == EventStatus.PUBLISHED;
    }

    public boolean isOngoing() {
        return status == EventStatus.ONGOING;
    }

    public boolean isCompleted() {
        return status == EventStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == EventStatus.CANCELLED;
    }

    public boolean isChangeRequested() {
        return status == EventStatus.CHANGE_REQUESTED;
    }

    public boolean canRegister() {
        return status == EventStatus.PUBLISHED &&
                startDate != null &&
                startDate.isAfter(LocalDateTime.now());
    }

    public boolean canEdit() {
        return status == EventStatus.DRAFT ||
                status == EventStatus.CHANGE_REQUESTED ||
                status == EventStatus.REJECTED;
    }

    public boolean canSubmitForReview() {
        return status == EventStatus.DRAFT || status == EventStatus.CHANGE_REQUESTED;
    }

    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        switch (status) {
            case DRAFT:
                return "Draft";
            case PENDING:
                return "Pending Review";
            case APPROVED:
                return "Approved";
            case REJECTED:
                return "Rejected";
            case PUBLISHED:
                return "Published";
            case ONGOING:
                return "Ongoing";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            case CHANGE_REQUESTED:
                return "Changes Requested";
            default:
                return status.toString();
        }
    }

    public String getStatusColor() {
        if (status == null) return "secondary";
        switch (status) {
            case DRAFT:
                return "info";
            case PENDING:
                return "warning";
            case APPROVED:
                return "success";
            case REJECTED:
                return "danger";
            case PUBLISHED:
                return "primary";
            case ONGOING:
                return "warning";
            case COMPLETED:
                return "success";
            case CANCELLED:
                return "dark";
            case CHANGE_REQUESTED:
                return "info";
            default:
                return "secondary";
        }
    }

    // Auto-update status based on event timing
    public EventStatus calculateCurrentStatus() {
        if (status == EventStatus.PUBLISHED) {
            LocalDateTime now = LocalDateTime.now();
            if (startDate != null && endDate != null) {
                if (now.isBefore(startDate)) {
                    return EventStatus.PUBLISHED; // Still upcoming
                } else if (now.isAfter(startDate) && now.isBefore(endDate)) {
                    return EventStatus.ONGOING; // Currently happening
                } else if (now.isAfter(endDate)) {
                    return EventStatus.COMPLETED; // Finished
                }
            }
        }
        return status; // Return current status if no change needed
    }

    @PreUpdate
    public void preUpdate() {
        // Auto-update status based on timing when entity is updated
        EventStatus calculatedStatus = calculateCurrentStatus();
        if (calculatedStatus != status &&
                (status == EventStatus.PUBLISHED || status == EventStatus.ONGOING)) {
            this.status = calculatedStatus;
        }
    }

    public enum EventStatus {
        DRAFT,          // Event đang được tạo
        PENDING,        // Chờ duyệt
        APPROVED,       // Đã duyệt
        REJECTED,       // Bị từ chối
        PUBLISHED,      // Đã xuất bản (có thể đăng ký)
        ONGOING,        // Đang diễn ra
        COMPLETED,      // Đã hoàn thành
        CANCELLED,      // Đã hủy
        CHANGE_REQUESTED // Yêu cầu thay đổi
    }
}

