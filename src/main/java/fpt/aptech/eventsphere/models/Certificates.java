package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Certificates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private int certificateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @Column(name = "certificate_url", length = 255, nullable = false)
    private String certificateUrl;

    @Column(name = "issued_on", nullable = false)
    private LocalDateTime issuedOn;
}