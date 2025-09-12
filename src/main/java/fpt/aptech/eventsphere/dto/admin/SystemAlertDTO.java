package fpt.aptech.eventsphere.dto.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemAlertDTO {
    private String id;
    private String type; // INFO, WARNING, ERROR, CRITICAL
    private String title;
    private String message;
    private String icon;
    private String color;
    private LocalDateTime timestamp;
    private boolean isRead;
    private String actionUrl;
}