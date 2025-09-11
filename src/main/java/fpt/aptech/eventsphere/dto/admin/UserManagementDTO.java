package fpt.aptech.eventsphere.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserManagementDTO {
    private int userId;
    private String email;
    private String fullName;
    private String phone;
    private String department;
    private String enrollmentNo;
    private boolean isActive;
    private boolean isDeleted;
    private String googleId;
    private LocalDateTime createdAt;
    private List<String> roles;

    // Profile details
    private String avatar;
    private String address;

    // Statistics
    private BigDecimal eventsAttended;
    private BigDecimal eventsOrganized;
    private BigDecimal averageRating;
}