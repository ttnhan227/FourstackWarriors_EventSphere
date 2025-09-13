package fpt.aptech.eventsphere.dto;

import fpt.aptech.eventsphere.models.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Notification entities.
 * Used to control the data exposed by the API and handle client-server communication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;

    /**
     * Converts a Notification entity to a NotificationDto
     */
    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .actionUrl(notification.getActionUrl())
                .build();
    }

    /**
     * Creates a NotificationDto for a new notification
     */
    public static NotificationDto create(
            String title,
            String message,
            Notification.NotificationType type,
            Long relatedEntityId,
            String relatedEntityType,
            String actionUrl) {
        
        return NotificationDto.builder()
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .actionUrl(actionUrl)
                .build();
    }
}
