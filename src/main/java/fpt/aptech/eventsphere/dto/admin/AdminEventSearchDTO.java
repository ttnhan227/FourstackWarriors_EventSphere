package fpt.aptech.eventsphere.dto.admin;

import fpt.aptech.eventsphere.models.Events;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AdminEventSearchDTO {
    private String keyword;
    private String category;
    private Events.EventStatus status;
    private String organizerName;
    private String organizerEmail;
    private String venueName;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDateTo;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateTo;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "startDate";

    @Builder.Default
    private String sortDirection = "desc";

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty() && !"all".equals(category);
    }

    public boolean hasStatus() {
        return status != null;
    }

    public boolean hasOrganizer() {
        return organizerName != null && !organizerName.trim().isEmpty() || organizerEmail != null && !organizerEmail.trim().isEmpty();
    }

    public boolean hasVenue() {
        return venueName != null && !venueName.trim().isEmpty();
    }

    public boolean hasDate() {
        return startDateFrom != null || startDateTo != null || endDateFrom != null || endDateTo != null;
    }

}
