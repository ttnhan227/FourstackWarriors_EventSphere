package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Roles, String> {

    Optional<Roles> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    List<Roles> findByRoleNameIn(List<String> roleNames);
}