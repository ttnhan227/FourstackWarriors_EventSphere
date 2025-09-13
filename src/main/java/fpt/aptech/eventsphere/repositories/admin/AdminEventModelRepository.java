package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.admin.EventsModel;
import fpt.aptech.eventsphere.models.admin.EventsModel.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository

public interface AdminEventModelRepository extends JpaRepository<EventsModel, Integer> {
    Optional<EventsModel> findByEventEventId(Integer eventId);
    @Query("SELECT em FROM EventsModel em WHERE em.status = :status ORDER BY em.submitAt DESC")
    List<EventsModel> findByStatusOrderBySubmitAtDesc(@Param("status") Status status);

    @Query("SELECT em FROM EventsModel em WHERE em.status = :status ORDER BY em.submitAt ASC")
    List<EventsModel> findByStatusOrderBySubmitAtAsc(@Param("status") Status status);

    long countByStatus(Status status);

    @Query("SELECT em.eventModelId, em.status, em.submitAt, em.updatedAt, em.adminComment, em.organnizerComment, " +
            "e.eventId, e.title, e.description, e.category, e.startDate, e.endDate, e.imageUrl, " +
            "v.name, o.userId, COALESCE(od.fullName, o.email), o.email, " +
            "es.totalSeats, es.seatsBooked, es.waitlistEnabled, " +
            "COALESCE(rb.email, ''), em.createdAt " +
            "FROM EventsModel em " +
            "JOIN em.event e " +
            "LEFT JOIN e.venue v " +
            "LEFT JOIN e.organizer o " +
            "LEFT JOIN o.userDetails od " +
            "LEFT JOIN e.eventSeating es " +
            "LEFT JOIN em.reviewBy rb " +
            "WHERE (:status IS NULL OR em.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "    LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(e.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR :category = '' OR e.category = :category) " +
            "AND (:organizerName IS NULL OR :organizerName = '' OR " +
            "    LOWER(COALESCE(od.fullName, o.email)) LIKE LOWER(CONCAT('%', :organizerName, '%')))")
    List<Object[]> searchEventsModel(
            @Param("status") Status status,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("organizerName") String organizerName
    );

    @Query("SELECT COUNT(em) FROM EventsModel em " +
            "JOIN em.event e " +
            "LEFT JOIN e.organizer o " +
            "LEFT JOIN o.userDetails od " +
            "WHERE (:status IS NULL OR em.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "    LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "    LOWER(e.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR :category = '' OR e.category = :category) " +
            "AND (:organizerName IS NULL OR :organizerName = '' OR " +
            "    LOWER(COALESCE(od.fullName, o.email)) LIKE LOWER(CONCAT('%', :organizerName, '%')))")

    long countEventsModel(
            @Param("status") Status status,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("organizerName") String organizerName
    );

    //active
    @Query("SELECT em FROM EventsModel em ORDER BY em.submitAt DESC")
    List<EventsModel> findRecentSubmissions(Status status);

    @Query("SELECT em FROM EventsModel em WHERE em.updatedAt >= :since ORDER BY em.updatedAt DESC")
    List<EventsModel> findRecentReviews(@Param("since") LocalDateTime since);
}

