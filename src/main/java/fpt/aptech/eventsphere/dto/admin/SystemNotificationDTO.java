package fpt.aptech.eventsphere.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemNotificationDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 1000, message = "Nội dung không được vượt quá 1000 ký tự")
    private String message;

    private String notificationType; // SYSTEM, WARNING, INFO, SUCCESS
    private String targetType; // ALL, ROLE_BASED, SPECIFIC_USERS, DEPARTMENT

    // Targeting options
    private List<String> targetRoles;
    private List<String> targetDepartments;
    private List<Integer> targetUserIds;

    private boolean isUrgent;
    private LocalDateTime scheduledAt;
    private LocalDateTime expiresAt;

    // Delivery options
    private boolean sendEmail;
    private boolean sendInApp;
    private boolean sendSMS;
}