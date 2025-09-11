package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venues venue;

    @Column(name = "total_seats", nullable = false)
    @Min(value = 1, message = "Total seats must be greater than 0")
    private int totalSeats;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked = 0;

    @Transient
    public int getAvailableSeat(){
        return totalSeats - seatsBooked;
    }

    @Column(name = "waitlist_enabled", nullable = false)
    private boolean waitlistEnabled;
}
