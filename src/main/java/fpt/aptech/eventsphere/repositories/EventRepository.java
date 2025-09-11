package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.EventSeating;
import fpt.aptech.eventsphere.models.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {
    Logger logger = LoggerFactory.getLogger(EventRepository.class);
    @Query("select e from Events e join e.organizer o join o.roles r where o.email = :email and r.roleId = 2")
    List<Events> findEventsByOrganizer(@Param("email")String email);

    @Query("select e from Events e where e.eventId = :id")
    Events findByEventId(@Param("id") int id);

    @Query("select e from Events e where e.venue.venueId = :id")
    List<Events> findEventsByVenueId(@Param("id") int id);
    
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
}
