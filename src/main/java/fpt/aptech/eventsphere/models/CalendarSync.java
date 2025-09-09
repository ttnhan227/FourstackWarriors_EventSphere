package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_sync")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_id")
    private int syncId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @Column(name = "calendar_type", length = 50, nullable = false)
    private String calendarType;

    @Column(name = "sync_timestamp", nullable = false)
    private LocalDateTime syncTimestamp;

    @Column(name = "calendar_url", length = 255)
    private String calendarUrl;
}