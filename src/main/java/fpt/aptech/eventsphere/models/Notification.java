package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a notification sent to users about various events and updates.
 * Notifications are linked to specific entities (like Events, Registrations, etc.)
 * and can be of different types based on the user action or system event.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;
    
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @Column(name = "action_url")
    private String actionUrl;
    
    @Builder.Default
    @Column(name = "is_email_sent", nullable = false)
    private boolean isEmailSent = false;
    
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;
    
    @Builder.Default
    @Column(name = "is_push_sent", nullable = false)
    private boolean isPushSent = false;
    
    @Column(name = "push_sent_at")
    private LocalDateTime pushSentAt;
    
    /**
     * Types of notifications that can be sent to participants
     */
    public enum NotificationType {
        // Event-related notifications
        EVENT_REGISTRATION,      // When user registers for an event
        EVENT_CANCELLATION,      // When user cancels registration
        EVENT_REMINDER,          // Reminder before event starts
        EVENT_UPDATED,           // When event details change
        EVENT_CANCELLED,         // When event is cancelled
        
        // Certificate notifications
        CERTIFICATE_AVAILABLE,   // When certificate is ready
        CERTIFICATE_REMINDER,    // Reminder to download certificate
        
        // System notifications
        SYSTEM_ALERT,            // Important system messages
        FEEDBACK_REQUEST         // Request for event feedback
    }
    
    /**
     * Marks the notification as read
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
    
    /**
     * Creates a notification for event registration
     */
    public static Notification forEventRegistration(Users user, Events event, Registrations registration) {
        return Notification.builder()
            .user(user)
            .title("Registration Confirmed")
            .message(String.format("You have successfully registered for %s", event.getTitle()))
            .type(NotificationType.EVENT_REGISTRATION)
            .relatedEntityId((long) registration.getRegistrationId())
            .relatedEntityType("REGISTRATION")
            .actionUrl(String.format("/events/%d", event.getEventId()))
            .build();
    }
    
    /**
     * Creates a reminder notification for an upcoming event
     */
    public static Notification forEventReminder(Users user, Events event) {
        return Notification.builder()
            .user(user)
            .title("Event Reminder")
            .message(String.format("Don't forget! %s starts soon!", event.getTitle()))
            .type(NotificationType.EVENT_REMINDER)
            .relatedEntityId((long) event.getEventId())
            .relatedEntityType("EVENT")
            .actionUrl(String.format("/events/%d", event.getEventId()))
            .build();
    }
}
