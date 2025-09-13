package fpt.aptech.eventsphere.dto.admin;

import fpt.aptech.eventsphere.models.admin.EventsModel.Status;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventModelSearchDTO {
    private String keyword;
    private Status status = Status.PENDING;
    private String category;
    private String organizerName;
    private String sortBy = "submitAt";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 10;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty() && !"all".equals(category);
    }

    public boolean hasOrganizer() {
        return organizerName != null && !organizerName.trim().isEmpty();
    }
}
