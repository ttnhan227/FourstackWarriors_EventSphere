package fpt.aptech.eventsphere.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_gallery")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private int mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    public enum FileType {
        IMAGE,
        VIDEO
    }

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Users uploadedBy;

    @Column(name = "caption", length = 150)
    private String caption;

    @Column(name = "uploaded_on", nullable = false)
    private LocalDateTime uploadedOn;
}