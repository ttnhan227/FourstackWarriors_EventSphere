package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Venues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venues, Integer> {
}
