package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import fpt.aptech.eventsphere.dto.admin.EventWithCountDTO;
import java.util.List;
import java.util.Optional;


public interface AdminEventRepository extends JpaRepository<Events, Integer> {

    Logger logger = LoggerFactory.getLogger(AdminEventRepository.class);

    @Query("""
                SELECT NEW fpt.aptech.eventsphere.dto.admin.EventWithCountDTO(
                    e,
                    (SELECT COUNT(r) FROM Registrations r WHERE r.event = e AND r.status = 'CONFIRMED')
                )
                FROM Events e 
                LEFT JOIN e.organizer o
                LEFT JOIN e.venue v
                WHERE ((:keyword IS NULL OR :keyword = '') OR (LOWER(e.title) LIKE %:keyword% OR LOWER(e.description) LIKE %:keyword%))
                AND ((:category IS NULL OR :category = 'all' OR :category = '') OR e.category = :category)
                AND ((:organizerName IS NULL OR :organizerName = '') OR (o.email IS NOT NULL AND LOWER(o.email) LIKE %:organizerName%))
                AND (:status IS NULL OR e.status = :status)
                GROUP BY e
            """)
    Page<EventWithCountDTO> searchEvents(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("organizerName") String organizerName,
            @Param("status") Events.EventStatus status,
            Pageable pageable
    );

    default Page<EventWithCountDTO> searchEventsWithLogging(String keyword, String category, String organizerName, Events.EventStatus status, Pageable pageable) {
        try {
            logger.debug("Executing searchEvents with params - keyword: '{}', category: '{}', organizerName: '{}', status: '{}', page: {}, size: {}, sort: {}",
                    keyword, category, organizerName, status,
                    pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort());

            Page<EventWithCountDTO> result = searchEvents(keyword, category, organizerName, status, pageable);
            
            // Log the results
            logger.debug("Found {} events ({} total)", result.getNumberOfElements(), result.getTotalElements());
            if (result.hasContent()) {
                EventWithCountDTO first = result.getContent().get(0);
                Events event = first.getEvent();
                logger.debug("First event - ID: {}, Title: '{}', Start Date: {}, Status: {}, Organizer: {}, Confirmed: {}",
                        event.getEventId(),
                        event.getTitle(),
                        event.getStartDate(),
                        event.getStatus(),
                        event.getOrganizer() != null ? event.getOrganizer().getEmail() : "null",
                        first.getConfirmedCount());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error in searchEventsWithLogging: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Query(value = "SELECT DISTINCT e.category FROM Events e ORDER BY e.category")
    List<String> findAllEventCategories();

    @Query("""
            SELECT NEW fpt.aptech.eventsphere.dto.admin.EventWithCountDTO(
                e,
                (SELECT COUNT(r) FROM Registrations r WHERE r.event = e AND r.status = 'CONFIRMED')
            )
            FROM Events e 
            LEFT JOIN e.organizer o 
            LEFT JOIN e.venue v 
            WHERE e.eventId = :eventId""")
    Optional<EventWithCountDTO> findEventWithRegistrations(@Param("eventId") int eventId);

    @Query("SELECT COUNT(e) FROM Events e WHERE e.status = :status")
    long countByStatus(@Param("status") Events.EventStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Events e SET e.title = :title, e.description = :description, e.category = :category, e.startDate = :startDate, e.endDate = :endDate, e.imageUrl = :imageUrl WHERE e.eventId = :eventId")
    int updateEventDetails(
            @Param("eventId") int eventId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("imageUrl") String imageUrl
    );

}
