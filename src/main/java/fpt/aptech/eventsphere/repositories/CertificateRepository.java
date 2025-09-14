package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Certificates;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificates, Integer> {
    
    // Find all certificates for a specific user
    List<Certificates> findByStudent(Users student);
    
    // Find a specific certificate for a user and event
    @Query("SELECT c FROM Certificates c WHERE c.student.userId = :userId AND c.event.eventId = :eventId")
    Optional<Certificates> findByStudent_UserIdAndEvent_EventId(
        @Param("userId") Integer userId, 
        @Param("eventId") Integer eventId
    );
    
    // Check if a certificate exists for a user and event
    boolean existsByStudent_UserIdAndEvent_EventId(Integer userId, Integer eventId);
    
    // Find all certificates for a user with event details
    @Query("SELECT c FROM Certificates c JOIN FETCH c.event e WHERE c.student.userId = :userId")
    List<Certificates> findWithEventDetailsByStudentId(@Param("userId") Integer userId);
    
    // Find all downloadable certificates (where certificate URL exists) for a user
    @Query("SELECT c FROM Certificates c WHERE c.student.userId = :userId AND c.certificateUrl IS NOT NULL")
    List<Certificates> findDownloadableCertificates(@Param("userId") Integer userId);
    
    @Query("SELECT c FROM Certificates c WHERE c.certificateId = :certificateId AND c.student.userId = :userId")
    Optional<Certificates> findByCertificateIdAndStudent_UserId(
        @Param("certificateId") Integer certificateId, 
        @Param("userId") Integer userId
    );
    
    // Find certificates by student ID and event ID
    @Query("SELECT c FROM Certificates c WHERE c.student.userId = :userId AND c.event.eventId = :eventId")
    Optional<Certificates> findByStudentIdAndEventId(
        @Param("userId") Integer userId, 
        @Param("eventId") Integer eventId
    );
    
    // Check if a certificate exists for a specific student and event
    boolean existsByStudentAndEvent(Users student, Events event);
    
    @Query("SELECT c FROM Certificates c WHERE c.student.userId = :userId AND c.event.eventId = :eventId AND c.isPaid = true")
    Optional<Certificates> findPaidCertificateByUserAndEvent(
        @Param("userId") Integer userId,
        @Param("eventId") Integer eventId
    );
    
    @Modifying
    @Query("UPDATE Certificates c SET c.isPaid = true, c.feeAmount = :feeAmount WHERE c.certificateId = :certificateId")
    void markAsPaid(@Param("certificateId") Integer certificateId, @Param("feeAmount") Double feeAmount);
    
    @Query("SELECT c FROM Certificates c WHERE c.student.userId = :userId AND c.isPaid = true")
    List<Certificates> findPaidCertificatesByUser(@Param("userId") Integer userId);
    
    @Query("SELECT c FROM Certificates c JOIN FETCH c.event e WHERE c.student.userId = :userId AND (c.isPaid = false OR c.certificateUrl IS NULL)")
    List<Certificates> findUnpaidOrPendingCertificates(@Param("userId") Integer userId);
    
    // Check if user has any certificate for an event
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Certificates c WHERE c.student.userId = :userId AND c.event.eventId = :eventId")
    boolean existsByUserIdAndEventId(
        @Param("userId") Integer userId, 
        @Param("eventId") Integer eventId
    );
    
    // Find all certificates for an event
    @Query("SELECT c FROM Certificates c WHERE c.event.eventId = :eventId")
    List<Certificates> findByEventId(@Param("eventId") Integer eventId);
    
    @Query("SELECT c FROM Certificates c WHERE c.event.eventId = :eventId")
    List<Certificates> findByEvent_EventId(@Param("eventId") Integer eventId);
    
    @Query("SELECT c FROM Certificates c WHERE c.certificateId = :certificateId AND c.student.userId = :userId")
    Optional<Certificates> findByIdAndStudent_UserId(
        @Param("certificateId") Integer certificateId, 
        @Param("userId") Integer userId
    );
    
    // Find certificates issued after a specific date
    @Query("SELECT c FROM Certificates c WHERE c.issuedOn >= :startDate")
    List<Certificates> findIssuedAfter(@Param("startDate") LocalDateTime startDate);
}
