package pl.exceptionhandled.reservationlab.event.service;

import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;

public interface EventSynchronizationService {
    void synchronizeEventCreate(EventCreatedMessage message);
}
