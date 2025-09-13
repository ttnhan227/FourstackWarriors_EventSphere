package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Events;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AdminEventManagRepo extends JpaRepository<Events, Integer> {
    @Query("""
        SELECT e FROM Events e 
        LEFT JOIN FETCH e.organizer o
        LEFT JOIN FETCH o.userDetails od
        LEFT JOIN FETCH e.venue v
        LEFT JOIN FETCH e.eventSeating es
        ORDER BY e.startDate DESC
        """)
    List<Events> findAllEventsWithDetails();

    @Query("""
        SELECT DISTINCT e FROM Events e 
        LEFT JOIN e.organizer o
        LEFT JOIN o.userDetails od
        LEFT JOIN e.venue v
        WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:category IS NULL OR LOWER(e.category) LIKE LOWER(CONCAT('%', :category, '%')))
        AND (:status IS NULL OR e.status = :status)
        AND (:organizerName IS NULL OR LOWER(od.fullName) LIKE LOWER(CONCAT('%', :organizerName, '%')))
        AND (:organizerEmail IS NULL OR LOWER(o.email) LIKE LOWER(CONCAT('%', :organizerEmail, '%')))
        AND (:venueName IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :venueName, '%')))
        AND (:startDateFrom IS NULL OR e.startDate >= :startDateFrom)
        AND (:startDateTo IS NULL OR e.startDate <= :startDateTo)
        AND (:endDateFrom IS NULL OR e.endDate >= :endDateFrom)
        AND (:endDateTo IS NULL OR e.endDate <= :endDateTo)
        """)
    Page<Events> searchEvents(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("status") Events.EventStatus status,
            @Param("organizerName") String organizerName,
            @Param("organizerEmail") String organizerEmail,
            @Param("venueName") String venueName,
            @Param("startDateFrom") LocalDateTime startDateFrom,
            @Param("startDateTo") LocalDateTime startDateTo,
            @Param("endDateFrom") LocalDateTime endDateFrom,
            @Param("endDateTo") LocalDateTime endDateTo,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Events e")
    Long countTotalEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.startDate <= CURRENT_TIMESTAMP AND e.endDate > CURRENT_TIMESTAMP")
    Long countOngoingEvents();

    @Query("SELECT COUNT(e) FROM Events e WHERE e.status = 'CANCELLED'")
    Long countCancelledEvents();

    @Query("""
        SELECT e FROM Events e 
        LEFT JOIN FETCH e.organizer o
        LEFT JOIN FETCH o.userDetails od
        LEFT JOIN FETCH e.venue v
        LEFT JOIN FETCH e.eventSeating es
        LEFT JOIN FETCH e.registrations r
        LEFT JOIN FETCH r.student
        LEFT JOIN FETCH e.attendances a
        LEFT JOIN FETCH e.feedbacks f
        WHERE e.eventId = :eventId
        """)
    Optional<Events> findEventWithAllDetails(@Param("eventId") Integer eventId);

    @Query("SELECT DISTINCT e.category FROM Events e WHERE e.category IS NOT NULL ORDER BY e.category")
    List<String> findAllCategories();

    @Query("SELECT e.status, COUNT(e) FROM Events e GROUP BY e.status")
    List<Object[]> getEventStatusStatistics();

    @Query("SELECT e.category, COUNT(e) FROM Events e WHERE e.category IS NOT NULL GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCategoryStatistics();

    @Query("""
        SELECT YEAR(e.startDate), MONTH(e.startDate), COUNT(e) 
        FROM Events e 
        WHERE e.startDate >= :fromDate 
        GROUP BY YEAR(e.startDate), MONTH(e.startDate) 
        ORDER BY YEAR(e.startDate) DESC, MONTH(e.startDate) DESC
        """)
    List<Object[]> getEventsByMonth(@Param("fromDate") LocalDateTime fromDate);

}
