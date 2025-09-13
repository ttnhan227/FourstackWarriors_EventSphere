package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Registrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registrations, Integer> {
}
