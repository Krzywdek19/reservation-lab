package pl.exceptionhandled.reservationlab.eventservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.EventResponse;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedPublisher;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final EventCreatedPublisher eventCreatedPublisher;

    @Transactional
    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .name(request.name())
                .location(request.location())
                .startsAt(request.startsAt())
                .build();

        Event savedEvent = eventRepository.saveAndFlush(event);

        eventCreatedPublisher.publish(new EventCreatedMessage(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getLocation(),
                savedEvent.getStartsAt()
        ));

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getLocation(),
                savedEvent.getStartsAt(),
                savedEvent.getCreatedAt()
        );
    }
}
