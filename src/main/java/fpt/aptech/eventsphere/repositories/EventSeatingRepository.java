package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.EventSeating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventSeatingRepository extends JpaRepository<EventSeating, Integer> {
    @Query("select es from EventSeating es where es.event.eventId = :id")
    EventSeating findByEventId(@Param("id") int eventId);
}
