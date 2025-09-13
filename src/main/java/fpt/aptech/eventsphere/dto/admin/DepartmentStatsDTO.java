package fpt.aptech.eventsphere.dto.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDTO {
    private String departmentName;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalEvents;
    private Long completedEvents;
    private Double averageRating;
    private Long totalRegistrations;
}