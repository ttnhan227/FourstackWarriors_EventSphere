package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating IS NULL OR f.rating = 0")
    BigDecimal countPendingReviews();
    
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.rating > 0")
    Double getAverageRating();
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating < 3")
    long countLowRatingFeedback();
}
