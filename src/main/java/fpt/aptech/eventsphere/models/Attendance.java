package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private int attendanceId;

    // Many-to-one with Events
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    // Many-to-one with Users (student)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @Column(name = "attended", nullable = false)
    private boolean attended;

    @Column(name = "marked_on")
    private LocalDateTime markedOn;
}