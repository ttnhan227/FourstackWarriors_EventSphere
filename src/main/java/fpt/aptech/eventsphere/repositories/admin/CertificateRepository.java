package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Certificates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface CertificateRepository extends JpaRepository<Certificates, Integer> {
    //
    long count();

    @Query("SELECT COUNT(c) FROM Certificates c WHERE DATE(c.issuedOn) = :today")
    long countByIssuedToday(@Param("today") LocalDate today);

    @Query("SELECT COUNT(c) FROM Certificates c WHERE c.issuedOn >= :startOfMonth")
    long countByIssuedAfter(@Param("startOfMonth") LocalDateTime startDate);
}
