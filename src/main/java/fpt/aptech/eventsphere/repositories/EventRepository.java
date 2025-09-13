package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Registrations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {
    Logger logger = LoggerFactory.getLogger(EventRepository.class);

    @Query("select e from Events e join e.organizer o join o.roles r where o.email = :email and r.roleId = 2")
    Page<Events> findEventsByOrganizer(@Param("email") String email, Pageable pageable);

    @Query("select e from Events e where e.eventId = :id")
    Events findByEventId(@Param("id") int id);

    @Query("select e from Events e where e.venue.venueId = :id")
    Page<Events> findEventsByVenueId(@Param("id") int id, Pageable pageable);

    @Query("SELECT e FROM Events e WHERE e.startDate >= CURRENT_DATE ORDER BY e.startDate")
    default List<Events> findAllUpcomingEvents() {
        List<Events> events = _findAllUpcomingEvents();
        logger.info("Found {} upcoming events", events.size());
        return events;
    }

    @Query("SELECT e FROM Events e WHERE e.startDate >= CURRENT_DATE ORDER BY e.startDate")
    List<Events> _findAllUpcomingEvents();

    @Query("SELECT e FROM Events e WHERE e.endDate < CURRENT_DATE ORDER BY e.endDate DESC")
    default List<Events> findAllPastEvents() {
        List<Events> events = _findAllPastEvents();
        logger.info("Found {} past events", events.size());
        return events;
    }

    @Query("SELECT e FROM Events e WHERE e.endDate < CURRENT_DATE ORDER BY e.endDate DESC")
    List<Events> _findAllPastEvents();

    @Query("SELECT e FROM Events e WHERE e.startDate >= CURRENT_DATE AND e.category = :category ORDER BY e.startDate")
    List<Events> findUpcomingEventsByCategory(@Param("category") String category);

    @Query("SELECT e FROM Events e LEFT JOIN FETCH e.organizer LEFT JOIN FETCH e.venue WHERE e.eventId = :id")
    Events findByIdWithOrganizerAndVenue(@Param("id") int id);

    @Query("select r from Registrations r where r.event.eventId = :id")
    List<Registrations> findEventRegistrations(@Param("id") int id);

    @Query("SELECT e FROM Events e WHERE e.title = :title")
    Events findByTitle(@Param("title") String title);

    @Query("SELECT e FROM Events e WHERE e.startDate <= CURRENT_DATE and e.endDate > CURRENT_DATE ORDER BY e.startDate")
    List<Events> findCurrentEvents();

    @Query("""
                SELECT e 
                FROM Events e 
                JOIN e.organizer o 
                JOIN o.roles r 
                WHERE o.email = :email 
                  AND r.roleId = 2 
                  AND e.startDate >= CURRENT_TIMESTAMP 
                ORDER BY e.startDate
            """)
    List<Events> findUpcomingEventsByOrganizer(@Param("email") String email);

    @Query("""
                SELECT e 
                FROM Events e 
                JOIN e.organizer o 
                JOIN o.roles r 
                WHERE o.email = :email 
                  AND r.roleId = 2 
                  AND e.endDate < current_timestamp 
                ORDER BY e.endDate DESC
            """)
    List<Events> findPastEventsByOrganizer(@Param("email") String email);

    @Query("""
                SELECT e 
                FROM Events e 
                JOIN e.organizer o 
                JOIN o.roles r 
                WHERE o.email = :email 
                  AND r.roleId = 2 
                  AND e.startDate <= current_timestamp 
                  AND e.endDate > current_timestamp 
                ORDER BY e.startDate
            """)
    List<Events> findCurrentEventsByOrganizer(@Param("email") String email);

    @Query("SELECT r FROM Registrations r WHERE r.registrationId = :registrationId")
    Registrations findRegistrationById(@Param("registrationId") int registrationId);

    @Query("SELECT e FROM Events e WHERE e.startDate BETWEEN :now AND :limit AND e.reminderSent = false")
    List<Events> findEventsStartingSoon(@Param("now") LocalDateTime now,
                                        @Param("limit") LocalDateTime limit);
}
