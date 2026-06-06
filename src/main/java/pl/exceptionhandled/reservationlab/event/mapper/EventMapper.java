package pl.exceptionhandled.reservationlab.event.mapper;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.event.dto.EventResponse;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.service.CreateEventCommand;

import java.util.List;

@Component
public class EventMapper {

    public CreateEventCommand toCommand(CreateEventRequest request) {
        return new CreateEventCommand(
                request.name(),
                request.location(),
                request.startsAt()
        );
    }

    public EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getStartsAt(),
                event.getCreatedAt()
        );
    }

    public List<EventResponse> toResponseList(List<Event> events) {
        return events.stream()
                .map(this::toResponse)
                .toList();
    }
}
