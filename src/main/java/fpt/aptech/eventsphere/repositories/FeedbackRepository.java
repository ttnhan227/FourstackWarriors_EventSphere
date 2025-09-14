package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    @Query("SELECT f FROM Feedback f WHERE f.event.eventId = :eventId")
    List<Feedback> findByEventEventId(@Param("eventId") Integer eventId);

    @Query("SELECT f FROM Feedback f WHERE f.student.userId = :userId")
    List<Feedback> findByStudentUserId(@Param("userId") Integer userId);

    @Query("SELECT f FROM Feedback f WHERE f.event.eventId = :eventId AND f.student.userId = :userId")
    Optional<Feedback> findByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    @Query("SELECT f FROM Feedback f WHERE f.student.userId = :userId AND f.submittedOn >= :timeLimit")
    List<Feedback> findRecentFeedbackByUser(@Param("userId") Integer userId, @Param("timeLimit") LocalDateTime timeLimit);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.student.userId = :userId AND f.submittedOn >= :timeLimit")
    int countRecentFeedbackByUser(@Param("userId") Integer userId, @Param("timeLimit") LocalDateTime timeLimit);

    @Query("SELECT f FROM Feedback f")
    List<Feedback> findAllFeedBack();

}
