package fpt.aptech.eventsphere.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventManagementDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String organizerName;
    private String organizerEmail;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String category;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private boolean deleted;
    private String status;  // PENDING, APPROVED, REJECTED
    private String organizerPhone;
    private String organizerDepartment;
    private long confirmedRegistrations;  // Number of confirmed registrations

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrganizerPhone() {
        return organizerPhone;
    }

    public void setOrganizerPhone(String organizerPhone) {
        this.organizerPhone = organizerPhone;
    }

    public String getOrganizerDepartment() {
        return organizerDepartment;
    }

    public void setOrganizerDepartment(String organizerDepartment) {
        this.organizerDepartment = organizerDepartment;
    }
    
    public long getConfirmedRegistrations() {
        return confirmedRegistrations;
    }
    
    public void setConfirmedRegistrations(long confirmedRegistrations) {
        this.confirmedRegistrations = confirmedRegistrations;
    }
}
