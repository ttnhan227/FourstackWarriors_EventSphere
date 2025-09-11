package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Events {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int eventId;

    @NotBlank(message = "Title must not be empty")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @NotBlank(message = "Description must not be empty")
    @Size(max = 1024, message = "Description must not exceed 1024 characters")
    @Column(name = "description")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venues venue;

    // Many-to-one with Users (organizer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Users organizer;

    // One-to-many relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registrations> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    //one to one
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventSeating eventSeating;

    //set eventseating and set event
    public void setEventSeating(EventSeating eventSeating) {
        this.eventSeating = eventSeating;
        if (eventSeating != null) {
            eventSeating.setEvent(this);
        }
    }
}