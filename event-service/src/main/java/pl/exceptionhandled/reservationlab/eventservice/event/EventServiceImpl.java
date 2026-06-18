package pl.exceptionhandled.reservationlab.eventservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.EventResponse;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedPublisher;
import pl.exceptionhandled.reservationlab.eventservice.seat.Seat;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

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

        request.seatNumbers()
                .stream()
                .map(seatNumber -> Seat.builder()
                        .seatNumber(seatNumber)
                        .build())
                .forEach(event::addSeat);

        Event savedEvent = eventRepository.saveAndFlush(event);

        eventCreatedPublisher.publish(new EventCreatedMessage(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getLocation(),
                savedEvent.getStartsAt(),
                savedEvent.getSeats()
                        .stream()
                        .map(Seat::getSeatNumber)
                        .toList()
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