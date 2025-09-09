package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_seating")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventSeating {

    @Id
    @Column(name = "event_id")
    private int eventId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "event_id")
    private Events event;

    @Column(name = "venue_id", nullable = false)
    private int venueId;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked;

    @Transient
    private int seatsAvailable;

    @Column(name = "waitlist_enabled", nullable = false)
    private boolean waitlistEnabled;
}