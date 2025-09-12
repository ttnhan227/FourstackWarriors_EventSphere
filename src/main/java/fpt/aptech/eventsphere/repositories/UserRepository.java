
package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByEmail(String email);

    @Query("SELECT u FROM Users u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Users> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    Optional<Users> findByGoogleId(String googleId);

    Optional<Users> findByResetToken(String resetToken);

    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.isActive = true AND u.isDeleted = false")
    Optional<Users> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE u.isActive = true AND u.isDeleted = false")
    List<Users> findAllActiveUsers();

    List<Users> findByIsActiveFalse();

    List<Users> findByIsDeletedFalse();
}
