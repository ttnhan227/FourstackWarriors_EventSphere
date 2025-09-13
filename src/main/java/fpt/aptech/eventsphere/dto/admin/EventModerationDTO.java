package fpt.aptech.eventsphere.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventModerationDTO {
    private int eventId;
    private String eventTitle;
    private String description;
    private String organizerName;
    private String organizerEmail;
    private String status; // PENDING, APPROVED, REJECTED, CHANGES_REQUESTED
    private LocalDateTime submittedAt;
    private LocalDateTime eventDate;
    private String venue;
    private BigDecimal maxParticipants;

    // Moderation details
    private String moderationNotes;
    private String adminComments;
    private LocalDateTime lastModerated;
    private String moderatedBy;

    // Content flags
    private boolean hasInappropriateContent;
    private boolean needsFactChecking;
    private boolean hasMediaToReview;
}