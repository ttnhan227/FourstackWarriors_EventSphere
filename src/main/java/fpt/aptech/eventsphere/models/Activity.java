// New model: Activity.java
package fpt.aptech.eventsphere.models;

import fpt.aptech.eventsphere.validations.ValidDateRange;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private int activityId;

    @NotBlank(message = "Description must not be empty")
    @Size(max = 1024, message = "Description must not exceed 1024 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "start_time")
    @NotNull(message = "Please enter start time")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Future(message = "start time must be in future")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @NotNull(message = "Please enter end time")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Future(message = "end time must be in future")
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event;
}