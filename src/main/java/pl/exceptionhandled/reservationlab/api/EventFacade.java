package pl.exceptionhandled.reservationlab.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.dto.EventResponse;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.service.EventService;
import pl.exceptionhandled.reservationlab.mapper.EventMapper;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventFacade {

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventResponse createEvent(CreateEventRequest request) {
        Event event = eventService.createEvent(eventMapper.toCommand(request));
        return eventMapper.toResponse(event);
    }

    public EventResponse getEvent(UUID eventId) {
        return eventMapper.toResponse(eventService.getEvent(eventId));
    }

    public List<EventResponse> getEvents() {
        return eventMapper.toResponseList(eventService.getEvents());
    }
}