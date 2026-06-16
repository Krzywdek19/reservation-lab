package pl.exceptionhandled.reservationlab.eventservice.event;

import pl.exceptionhandled.reservationlab.eventservice.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.EventResponse;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request);
}