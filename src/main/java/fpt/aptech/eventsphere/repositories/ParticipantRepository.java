package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Users, Integer> {
    
    @Query("SELECT e FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.startDate >= CURRENT_DATE " +
           "ORDER BY e.startDate, e.endDate")
    List<Events> findUpcomingRegisteredEvents(@Param("userId") Integer userId);
    
    @Query("SELECT e FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.startDate < CURRENT_DATE " +
           "ORDER BY e.startDate DESC, e.endDate DESC")
    List<Events> findPastRegisteredEvents(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(r) FROM Registrations r WHERE r.student.userId = :userId")
    int countRegistrationsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT COUNT(DISTINCT e.eventId) FROM Events e JOIN Registrations r ON e.eventId = r.event.eventId " +
           "WHERE r.student.userId = :userId AND e.startDate < CURRENT_DATE")
    int countAttendedEvents(@Param("userId") Integer userId);
}
