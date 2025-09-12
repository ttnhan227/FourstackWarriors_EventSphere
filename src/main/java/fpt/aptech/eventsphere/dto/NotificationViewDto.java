package fpt.aptech.eventsphere.dto;

import fpt.aptech.eventsphere.models.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for displaying notifications in Thymeleaf views
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViewDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String timeAgo;
    private String actionUrl;

    /**
     * Converts a Notification entity to a NotificationViewDto
     */
    public static NotificationViewDto fromEntity(Notification notification) {
        return NotificationViewDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .timeAgo(formatTimeAgo(notification.getCreatedAt()))
                .actionUrl(notification.getActionUrl())
                .build();
    }

    /**
     * Formats the time as "X minutes/hours/days ago"
     */
    private static String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (minutes < 1440) return (minutes / 60) + "h ago";
        return (minutes / 1440) + "d ago";
    }
}
