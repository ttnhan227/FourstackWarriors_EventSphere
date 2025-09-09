package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {

    Optional<UserDetails> findByUser(Users user);

    Optional<UserDetails> findByUser_UserId(int userId);

    Optional<UserDetails> findByEnrollmentNo(String enrollmentNo);

    boolean existsByEnrollmentNo(String enrollmentNo);

    boolean existsByPhone(String phone);

    @Query("SELECT ud FROM UserDetails ud WHERE ud.fullName LIKE %:name%")
    List<UserDetails> findByFullNameContaining(@Param("name") String name);

    @Query("SELECT ud FROM UserDetails ud WHERE ud.department = :department")
    List<UserDetails> findByDepartment(@Param("department") String department);
}