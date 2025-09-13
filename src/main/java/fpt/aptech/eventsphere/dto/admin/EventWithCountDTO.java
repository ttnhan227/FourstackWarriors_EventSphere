package fpt.aptech.eventsphere.dto.admin;

import fpt.aptech.eventsphere.models.Events;

public class EventWithCountDTO {
    private final Events event;
    private final Long confirmedCount;

    public EventWithCountDTO(Events event, Long confirmedCount) {
        this.event = event;
        this.confirmedCount = confirmedCount;
    }

    public Events getEvent() {
        return event;
    }

    public Long getConfirmedCount() {
        return confirmedCount;
    }
}
