package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Users, Integer> {
    Logger logger = LoggerFactory.getLogger(ProfileRepository.class);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId AND r.status = 'CONFIRMED'")
    default long countAttendedEvents(@Param("userId") int userId) {
        long count = _countAttendedEvents(userId);
        logger.info("Found {} attended events for user ID: {}", count, userId);
        return count;
    }
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId AND r.status = 'CONFIRMED'")
    long _countAttendedEvents(@Param("userId") int userId);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId")
    default long countTotalRegistrations(@Param("userId") int userId) {
        long count = _countTotalRegistrations(userId);
        logger.info("Found {} total registrations for user ID: {}", count, userId);
        return count;
    }
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId")
    long _countTotalRegistrations(@Param("userId") int userId);
    
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.userDetails WHERE u.userId = :userId")
    Users findByIdWithDetails(@Param("userId") int userId);
}
