package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.EventSeating;
import fpt.aptech.eventsphere.models.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {
    @Query("select e from Events e join e.organizer o join o.roles r where o.email = :email and r.roleId = 2")
    List<Events> findEventsByOrganizer(@Param("email")String email);

    @Query("select e from Events e where e.eventId = :id")
    Events findByEventId(@Param("id") int id);

    @Query("select e from Events e where e.venue.venueId = :id")
    List<Events> findEventsByVenueId(@Param("id") int id);
}
