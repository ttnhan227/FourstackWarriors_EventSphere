package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.admin.EventsModel;
import fpt.aptech.eventsphere.models.admin.EventsModel.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminEventRepository extends JpaRepository<Events, Integer> {

    @Query("select count(e) from Events e where e.startDate > current_timestamp")
    long countPendEvent();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate <= CURRENT_TIMESTAMP AND e.endDate >= CURRENT_TIMESTAMP")
    long countApprovedEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.endDate < CURRENT_TIMESTAMP")
    long countRejectedEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate >= CURRENT_TIMESTAMP")
    long countUpcomingEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.endDate < CURRENT_TIMESTAMP")
    long countPastEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.endDate < CURRENT_TIMESTAMP")
    long countCompletedEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate <= CURRENT_TIMESTAMP AND e.endDate >= CURRENT_TIMESTAMP")
    long countOngoingEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE DATE(e.startDate) = :today")
    long countByDate(@Param("today") LocalDate today);

    @Query("SELECT COUNT(e) FROM Events e WHERE DATE(e.startDate) = :today")
    long countCreatedToday(@Param("today") LocalDate today);

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(ud.department, 'Unknown'), COUNT(e) FROM Events e LEFT JOIN e.organizer o LEFT JOIN o.userDetails ud GROUP BY ud.department")
    List<Object[]> countEventsByDepartment();

    @Query("SELECT AVG(CAST(f.rating AS double)) FROM Events e JOIN e.feedbacks f WHERE f.rating > 0")
    Double getAverageEventRating();

    // Chart Data
    @Query("SELECT DATE(e.startDate) as date, COUNT(e) as count FROM Events e WHERE e.startDate >= :startDate GROUP BY DATE(e.startDate) ORDER BY DATE(e.startDate)")
    List<Object[]> getEventCreationStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT YEAR(e.startDate) as year, MONTH(e.startDate) as month, COUNT(e) as count FROM Events e WHERE e.startDate >= :startDate GROUP BY YEAR(e.startDate), MONTH(e.startDate) ORDER BY YEAR(e.startDate), MONTH(e.startDate)")
    List<Object[]> getMonthlyEventStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate >= :startDate AND e.startDate < :endDate")
    long countEventsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Events e WHERE e.startDate >= :today ORDER BY e.startDate ASC")
    List<Events> findUpcomingEvents(@Param("today") LocalDateTime today);

    @Query("SELECT e FROM Events e WHERE e.endDate < :today ORDER BY e.endDate DESC")
    List<Events> findPastEvents(@Param("today") LocalDateTime today);

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate = CURRENT_DATE")
    long countTodayEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate >= :weekStart AND e.startDate < :weekEnd")
    long countThisWeekEvents(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);

    @Query("SELECT COUNT(e) FROM Events e WHERE YEAR(e.startDate) = YEAR(CURRENT_DATE) AND MONTH(e.startDate) = MONTH(CURRENT_DATE)")
    long countThisMonthEvents();

    List<Events> findAllByStatus(Events.EventStatus status);
}
