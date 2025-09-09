package fpt.aptech.eventsphere.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @NotBlank(message = "Role name must not be empty")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name can only contain uppercase letters and underscores")
    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;

    public Roles(String roleName) {
        this.roleName = roleName;
    }
}
