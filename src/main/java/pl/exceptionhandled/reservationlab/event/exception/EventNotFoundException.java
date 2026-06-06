package pl.exceptionhandled.reservationlab.event.exception;

import pl.exceptionhandled.reservationlab.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(UUID eventId) {
        super("Event not found: " + eventId);
    }
}