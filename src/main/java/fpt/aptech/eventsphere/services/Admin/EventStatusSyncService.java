package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.admin.EventsModel;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminEventModelRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class EventStatusSyncService {

    @Autowired
    private AdminEventRepository eventRepository;
    
    @Autowired
    private AdminEventModelRepository eventModelRepository;

    public void syncEventStatus(Events event, EventsModel.Status modelStatus) {
        Events.EventStatus newEventStatus = convertToEventStatus(modelStatus);
        
        Events.EventStatus oldStatus = event.getStatus();
        event.setStatus(newEventStatus);
        
        logStatusChange(event.getEventId(), oldStatus, newEventStatus);
        
        applyStatusChangeLogic(event, oldStatus, newEventStatus);
        
        eventRepository.save(event);
    }

    public void syncModelStatus(EventsModel eventModel, Events.EventStatus eventStatus) {
        EventsModel.Status newModelStatus = convertToModelStatus(eventStatus);
        
        EventsModel.Status oldStatus = eventModel.getStatus();
        eventModel.setStatus(newModelStatus);
        eventModel.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated model
        eventModelRepository.save(eventModel);
    }

    public void syncBidirectional(Events event, EventsModel eventModel, EventsModel.Status newStatus) {
        eventModel.setStatus(newStatus);
        eventModel.setUpdatedAt(LocalDateTime.now());
        
        syncEventStatus(event, newStatus);
        
        eventModelRepository.save(eventModel);
        eventRepository.save(event);
    }

    public void autoPublishApprovedEvent(Events event) {
        if (event.isApproved() && 
            event.getStartDate() != null && 
            event.getStartDate().isAfter(LocalDateTime.now())) {
            
            event.setStatus(Events.EventStatus.PUBLISHED);
            eventRepository.save(event);
            
            logStatusChange(event.getEventId(), Events.EventStatus.APPROVED, Events.EventStatus.PUBLISHED);
        }
    }

    public void updateEventStatusByTiming() {
        LocalDateTime now = LocalDateTime.now();

        eventRepository.findAllByStatus(Events.EventStatus.PUBLISHED)
                .stream()
                .filter(event -> event.getStartDate() != null &&
                           event.getStartDate().isBefore(now) && 
                           event.getEndDate() != null && 
                           event.getEndDate().isAfter(now))
                .forEach(event -> {
                event.setStatus(Events.EventStatus.ONGOING);
                eventRepository.save(event);
                logStatusChange(event.getEventId(), Events.EventStatus.PUBLISHED, Events.EventStatus.ONGOING);
            });

        eventRepository.findAllByStatus(Events.EventStatus.ONGOING)
            .stream()
            .filter(event -> event.getEndDate() != null && 
                           event.getEndDate().isBefore(now))
            .forEach(event -> {
                event.setStatus(Events.EventStatus.COMPLETED);
                eventRepository.save(event);
                logStatusChange(event.getEventId(), Events.EventStatus.ONGOING, Events.EventStatus.COMPLETED);
                
                // Also update EventsModel if exists
                Optional<EventsModel> modelOpt = eventModelRepository.findByEventEventId(event.getEventId());
                if (modelOpt.isPresent()) {
                    EventsModel model = modelOpt.get();
                    model.setStatus(EventsModel.Status.FINISHED);
                    model.setUpdatedAt(LocalDateTime.now());
                    eventModelRepository.save(model);
                }
            });
    }

    private Events.EventStatus convertToEventStatus(EventsModel.Status modelStatus) {
        switch (modelStatus) {
            case PENDING:
                return Events.EventStatus.PENDING;
            case APPROVED:
                return Events.EventStatus.APPROVED;
            case REJECTED:
                return Events.EventStatus.REJECTED;
            case CANCELLED:
                return Events.EventStatus.CANCELLED;
            case FINISHED:
                return Events.EventStatus.COMPLETED;
            case CHANGE_REQUESTED:
                return Events.EventStatus.CHANGE_REQUESTED;
            default:
                return Events.EventStatus.DRAFT;
        }
    }

    private EventsModel.Status convertToModelStatus(Events.EventStatus eventStatus) {
        switch (eventStatus) {
            case PENDING:
                return EventsModel.Status.PENDING;
            case APPROVED:
                return EventsModel.Status.APPROVED;
            case REJECTED:
                return EventsModel.Status.REJECTED;
            case CANCELLED:
                return EventsModel.Status.CANCELLED;
            case COMPLETED:
                return EventsModel.Status.FINISHED;
            case CHANGE_REQUESTED:
                return EventsModel.Status.CHANGE_REQUESTED;
            default:
                return EventsModel.Status.PENDING;
        }
    }

    private void applyStatusChangeLogic(Events event, Events.EventStatus oldStatus, Events.EventStatus newStatus) {
        if (newStatus == Events.EventStatus.APPROVED) {
        }
        
        if (newStatus == Events.EventStatus.REJECTED) {
        }

        if (newStatus == Events.EventStatus.CANCELLED) {
        }

        if (newStatus == Events.EventStatus.COMPLETED) {
        }
    }

    private void logStatusChange(int eventId, Events.EventStatus oldStatus, Events.EventStatus newStatus) {
        System.out.println(String.format(
            "[EVENT STATUS CHANGE] Event ID: %d, Old Status: %s, New Status: %s, Time: %s",
            eventId,
            oldStatus != null ? oldStatus.name() : "NULL",
            newStatus != null ? newStatus.name() : "NULL",
            LocalDateTime.now()
        ));
    }

    public boolean isStatusSynced(Events event, EventsModel eventModel) {
        if (event == null || eventModel == null) return false;
        
        Events.EventStatus expectedEventStatus = convertToEventStatus(eventModel.getStatus());
        return event.getStatus() == expectedEventStatus;
    }

    @Transactional
    public void forceFullSync() {
        eventModelRepository.findAll().forEach(model -> {
            if (model.getEvent() != null) {
                syncEventStatus(model.getEvent(), model.getStatus());
            }
        });
    }
}
