package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_share_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventShareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private int shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @Column(name = "platform", length = 50, nullable = false)
    private String platform;

    @Column(name = "share_timestamp", nullable = false)
    private LocalDateTime shareTimestamp;

    @Lob
    @Column(name = "share_message")
    private String shareMessage;
}