package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {
}
