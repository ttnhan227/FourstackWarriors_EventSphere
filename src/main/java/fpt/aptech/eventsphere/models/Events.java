package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Size(max = 100, message = "Venue must not exceed 100 characters")
    @Column(name = "venue", length = 100)
    private String venue;

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
}