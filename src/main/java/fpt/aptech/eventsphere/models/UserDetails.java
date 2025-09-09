package fpt.aptech.eventsphere.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "user_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private int detailId;

    @NotNull(message = "User must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @NotBlank(message = "Full name must not be empty")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Full name can only contain letters and spaces")
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Phone number must follow Vietnamese format")
    @Column(name = "phone", length = 15)
    private String phone;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Column(name = "department", length = 100)
    private String department;

    @Pattern(regexp = "^[A-Z0-9]{6,50}$", message = "Enrollment number must be 6–50 characters, containing only uppercase letters and digits")
    @Column(name = "enrollment_no", length = 50)
    private String enrollmentNo;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(
            regexp = "^(https?://.+\\.(jpg|jpeg|png|gif|webp)|/images/.+\\.(jpg|jpeg|png|gif|webp))?$",
            message = "Avatar must be a valid image URL (jpg, jpeg, png, gif, webp) or url")
    @Column(name = "avatar", length = 500)
    private String avatar;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", length = 255)
    private String address;

    public UserDetails(Users user) {
        this.user = user;
    }
}