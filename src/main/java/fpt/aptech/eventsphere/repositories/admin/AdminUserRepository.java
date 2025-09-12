
package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<Users, Integer> {

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
    @Query("SELECT COUNT(u) FROM Users u WHERE u.isActive = true AND u.isDeleted = false")
    long countByIsActiveTrueAndIsDeletedFalse();
    long countByIsDeletedTrue();
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM Users u WHERE DATE(u.createdAt) = :today")
    long countByCreatedAtToday(@Param("today") LocalDate today);

    @Query("SELECT ud.department, COUNT(u) FROM Users u JOIN u.userDetails ud WHERE ud.department IS NOT NULL GROUP BY ud.department")
    List<Object[]> countUsersByDepartment();
    
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM Users u WHERE u.createdAt >= :startDate GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT r.roleName, COUNT(u) FROM Users u JOIN u.roles r GROUP BY r.roleName")
    List<Object[]> countUsersByRole();

    //admin search sort....
    @Query("SELECT u.userId, u.email, " +
            "COALESCE(ud.fullName, ''), COALESCE(ud.phone, ''), " +
            "COALESCE(ud.department, ''), COALESCE(ud.enrollmentNo, ''), " +
            "u.isActive, u.isDeleted, COALESCE(u.googleId, ''), u.createdAt, " +
            "COALESCE(ud.avatar, ''), COALESCE(ud.address, '') " +
            "FROM Users u " +
            "LEFT JOIN u.userDetails ud " +
            "WHERE u.isDeleted = false " +
            "AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.roleName = 'ADMIN') " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.department, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.enrollmentNo, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:department IS NULL OR :department = '' OR ud.department = :department) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:role IS NULL OR :role = '' OR EXISTS " +
            "    (SELECT r FROM u.roles r WHERE r.roleName = :role))")
    List<Object[]> searchUsersForManagement(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("role") String role,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT DISTINCT ud.department FROM UserDetails ud WHERE ud.department IS NOT NULL ORDER BY ud.department")
    List<String> findAllDepartments();

    @Query("SELECT DISTINCT r.roleName FROM Roles r WHERE r.roleName != 'ADMIN' ORDER BY r.roleName")
    List<String> findAllRoles();

    @Query("SELECT COUNT(u) FROM Users u " +
            "LEFT JOIN u.userDetails ud " +
            "WHERE u.isDeleted = false " +
            "AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.roleName = 'ADMIN') " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.department, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(COALESCE(ud.enrollmentNo, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:department IS NULL OR :department = '' OR ud.department = :department) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:role IS NULL OR :role = '' OR EXISTS " +
            "    (SELECT r FROM u.roles r WHERE r.roleName = :role))")
    long countSearchUsersForManagement(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("role") String role,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT r.roleName FROM Users u JOIN u.roles r WHERE u.userId = :userId")
    List<String> findRolesByUserId(@Param("userId") int userId);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.isDeleted = false AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.roleName = 'ADMIN')")
    long countByIsDeletedFalseAndNotAdmin();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.isActive = true AND u.isDeleted = false AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.roleName = 'ADMIN')")
    long countByIsActiveTrueAndIsDeletedFalseAndNotAdmin();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.isActive = false AND u.isDeleted = false AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.roleName = 'ADMIN')")
    long countByIsActiveFalseAndIsDeletedFalseAndNotAdmin();

    long countByIsDeletedFalse();


}