package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Users, Integer> {
    
    @Query("SELECT e FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.startDate >= CURRENT_DATE " +
           "AND (r.status = 'CONFIRMED' OR r.status = 'PENDING') " +
           "ORDER BY e.startDate")
    List<Events> findUpcomingRegisteredEvents(@Param("userId") Integer userId);
    
    @Query("SELECT e FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.endDate < CURRENT_DATE " +
           "ORDER BY e.endDate DESC")
    List<Events> findPastRegisteredEvents(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId")
    int countRegistrationsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(DISTINCT e.eventId) FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.startDate < CURRENT_DATE")
    int countAttendedEvents(@Param("userId") Integer userId);
    
    // New methods for event registration
    @Query("SELECT r FROM Registrations r WHERE r.event.eventId = :eventId AND r.student.userId = :userId")
    Optional<Registrations> findRegistration(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.event.eventId = :eventId AND r.status = 'CONFIRMED'")
    int countConfirmedRegistrations(@Param("eventId") Integer eventId);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.event.eventId = :eventId")
    int countTotalRegistrations(@Param("eventId") Integer eventId);
    
    @Query("SELECT r FROM Registrations r JOIN FETCH r.event e WHERE r.student.userId = :userId AND e.endDate >= CURRENT_DATE")
    List<Registrations> findUpcomingRegistrations(@Param("userId") Integer userId);
    
    @Modifying
    @Query("DELETE FROM Registrations r WHERE r.registrationId = :registrationId")
    void deleteRegistration(@Param("registrationId") Integer registrationId);
}
