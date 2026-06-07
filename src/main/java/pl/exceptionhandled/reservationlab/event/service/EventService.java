package pl.exceptionhandled.reservationlab.event.service;

import jakarta.validation.Valid;
import pl.exceptionhandled.reservationlab.event.Event;

import java.util.List;
import java.util.UUID;

public interface EventService {
    Event createEvent(@Valid CreateEventCommand command);
    Event getEvent(UUID eventId);
    List<Event> getEvents();
}
