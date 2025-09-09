package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Registrations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private int registrationId;

    // Many-to-one with Events
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    // Many-to-one with Users (student)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @Column(name = "registered_on", nullable = false)
    private LocalDateTime registeredOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status;

    public enum RegistrationStatus {
        CONFIRMED,
        CANCELLED,
        WAITLIST
    }
}