package fpt.aptech.eventsphere.dto;

import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Integer userId;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Full name can only contain letters and spaces")
    private String fullName;
    
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Phone number must follow Vietnamese format")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phone;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @Size(max = 50, message = "Enrollment number must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9]{0,50}$", message = "Enrollment number can only contain uppercase letters and digits")
    private String enrollmentNo;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(
        regexp = "^(https?://.+\\.(jpg|jpeg|png|gif|webp)|/images/.+\\.(jpg|jpeg|png|gif|webp))?$",
        message = "Avatar must be a valid image URL (jpg, jpeg, png, gif, webp) or url")
    private String avatar;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    private LocalDateTime memberSince;
    private Integer totalEventsAttended = 0;
    private Integer totalEventsRegistered = 0;
    
    public static ProfileDto fromEntity(Users user, UserDetails details) {
        ProfileDto dto = new ProfileDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setMemberSince(user.getCreatedAt());
        
        if (details != null) {
            dto.setFullName(details.getFullName());
            dto.setPhone(details.getPhone());
            dto.setDepartment(details.getDepartment());
            dto.setEnrollmentNo(details.getEnrollmentNo());
            dto.setAvatar(details.getAvatar());
            dto.setAddress(details.getAddress());
        }
        
        return dto;
    }
    
    public void updateEntity(UserDetails details) {
        if (details != null) {
            details.setFullName(this.fullName);
            details.setPhone(this.phone);
            details.setDepartment(this.department);
            details.setEnrollmentNo(this.enrollmentNo);
            details.setAvatar(this.avatar);
            details.setAddress(this.address);
        }
    }
}
