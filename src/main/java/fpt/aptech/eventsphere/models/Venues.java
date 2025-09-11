package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "venues")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Venues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private int venueId;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "name cannot be blank")
    private String name;
}
