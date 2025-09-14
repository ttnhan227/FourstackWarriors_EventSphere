package fpt.aptech.eventsphere.models;

import fpt.aptech.eventsphere.validations.NoOffensiveLanguage;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int feedbackId;

    // Many-to-one with Events
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    // Many-to-one with Users (student)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @Column(name = "rating", nullable = false)
    private int rating;  // 1 to 5

    @Lob
    @NoOffensiveLanguage
    @Column(name = "comments")
    private String comments;

    @Column(name = "submitted_on")
    private LocalDateTime submittedOn;
}