package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, length = 100, nullable = false)
    private String email;

//    password hash
    @Size(max = 255, message = "Password must not exceed 255 characters")
    @Column(name = "password", length = 255)
    private String password;

    // xac thuc email
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Size(max = 255, message = "Google ID must not exceed 255 characters")
    @Column(name = "google_id")
    private String googleId;

    //reset password
    @Size(max = 255, message = "Reset token must not exceed 255 characters")
    @Column(name = "reset_token")
    private String resetToken;

    @Future(message = "Token expiry time must be in the future")
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // user avaliable
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // Quan hệ với UserDetails
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserDetails userDetails;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registrations> registrations = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    // n-n với Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Roles> roles = new ArrayList<>();
}
