package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.AdminEventDTO;
import fpt.aptech.eventsphere.dto.admin.AdminEventSearchDTO;
import fpt.aptech.eventsphere.models.Events;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public interface AdminEventManagementService {
    List<AdminEventDTO> findAllEvents();

    Page<AdminEventDTO> searchAndFilterEvents(AdminEventSearchDTO searchDTO);

    Optional<AdminEventDTO> getEventDetails(Integer eventId);

    Long getTotalEvents();
    Long getOngoingEvents();
    Long getCancelledEvents();

    Map<String, Long> getEventStatusStatistics();
    Map<String, Long> getEventCategoryStatistics();
    Map<String, Long> getEventsByMonth(int months);

    List<String> getAllCategories();

    boolean deleteEvent(Integer eventId);

    boolean restoreEvent(Integer eventId);

    boolean updateEventStatus(Integer eventId, Events.EventStatus status);

    List<AdminEventDTO> getEventsForExport(AdminEventSearchDTO searchDTO);

}
