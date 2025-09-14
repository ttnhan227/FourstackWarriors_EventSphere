package fpt.aptech.eventsphere.repositories;

import fpt.aptech.eventsphere.models.Bookmark;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndEvent(Users user, Events event);
    Optional<Bookmark> findByUserAndEvent(Users user, Events event);
    Page<Bookmark> findByUser(Users user, Pageable pageable);
    void deleteByUserAndEvent(Users user, Events event);
}
