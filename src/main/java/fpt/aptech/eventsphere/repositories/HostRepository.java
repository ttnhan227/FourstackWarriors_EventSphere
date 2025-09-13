package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostRepository extends JpaRepository<Host, Integer> {
}