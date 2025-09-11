
package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Users> findByGoogleId(String googleId);

    Optional<Users> findByResetToken(String resetToken);

    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.isActive = true AND u.isDeleted = false")
    Optional<Users> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE u.isActive = true AND u.isDeleted = false")
    List<Users> findAllActiveUsers();

    List<Users> findByIsActiveFalse();
    List<Users> findByIsDeletedFalse();
    
    // Admin Dashboard Queries
    long countByIsActiveTrueAndIsDeletedFalse();
    long countByIsDeletedTrue();
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ud.department, COUNT(u) FROM Users u JOIN u.userDetails ud WHERE ud.department IS NOT NULL GROUP BY ud.department")
    List<Object[]> countUsersByDepartment();
    
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM Users u WHERE u.createdAt >= :startDate GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT r.roleName, COUNT(u) FROM Users u JOIN u.roles r GROUP BY r.roleName")
    List<Object[]> countUsersByRole();
}