package fpt.aptech.eventsphere.repositories.admin;

import fpt.aptech.eventsphere.models.MediaGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMediaGalleryRepository extends JpaRepository<MediaGallery, Integer> {

//    @Query("SELECT COUNT(m) FROM MediaGallery m WHERE m.status = 'PENDING'")
//    BigDecimal countPendingReviews();
//
//    @Query("SELECT COUNT(m) FROM MediaGallery m WHERE m.status = 'APPROVED'")
//    long countApprovedMedia();
//
//    @Query("SELECT COUNT(m) FROM MediaGallery m WHERE m.status = 'REJECTED'")
//    long countRejectedMedia();
}