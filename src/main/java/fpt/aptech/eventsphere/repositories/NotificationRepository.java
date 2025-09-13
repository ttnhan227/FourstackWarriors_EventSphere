package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Notification;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
    
    /**
     * Find paginated notifications for a user
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(Users user);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserAndIsReadFalse(Users user);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    void markAllAsRead(@Param("user") Users user);
    
    /**
     * Find notifications related to an event
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = 'EVENT' AND n.relatedEntityId = :eventId")
    List<Notification> findByEvent(@Param("eventId") Long eventId);
    
    /**
     * Find notifications related to a user's registration
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = 'REGISTRATION' AND n.relatedEntityId = :registrationId AND n.user = :user")
    List<Notification> findByUserAndRegistration(
            @Param("user") Users user,
            @Param("registrationId") Long registrationId);
    
    /**
     * Find notifications by type for a specific user
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(Users user, Notification.NotificationType type);
    
    /**
     * Find notifications that need email sending
     */
    @Query("SELECT n FROM Notification n WHERE n.isEmailSent = false AND n.user.email IS NOT NULL")
    List<Notification> findUnsentEmailNotifications();
    
    // Push notification related methods will be added when implementing push notifications
}
