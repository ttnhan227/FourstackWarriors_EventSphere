package fpt.aptech.eventsphere.dto.admin;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequestDTO {
    private String keyword;
    private String department;
    private String role;
    private Boolean isActive;
    private String sortBy = "userId";
    private String sortDirection = "asc";
    private int page = 0;
    private int size = 10;

    // Helper methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasDepartment() {
        return department != null && !department.trim().isEmpty() && !"all".equals(department);
    }

    public boolean hasRole() {
        return role != null && !role.trim().isEmpty() && !"all".equals(role);
    }

    public boolean hasActiveFilter() {
        return isActive != null;
    }
}
