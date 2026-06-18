package pl.exceptionhandled.reservationlab.eventservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.EventResponse;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxService;
import pl.exceptionhandled.reservationlab.eventservice.seat.Seat;
import pl.exceptionhandled.reservationlab.eventservice.seat.exception.DuplicatedSeatNumberException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final OutboxService outboxService;

    @Transactional
    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        validateSeatNumbers(request.seatNumbers());

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

        EventCreatedMessage eventCreatedMessage = new EventCreatedMessage(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getLocation(),
                savedEvent.getStartsAt(),
                savedEvent.getSeats()
                        .stream()
                        .map(Seat::getSeatNumber)
                        .toList()
        );

        outboxService.saveEventCreatedMessage(eventCreatedMessage);

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getLocation(),
                savedEvent.getStartsAt(),
                savedEvent.getCreatedAt()
        );
    }

    private void validateSeatNumbers(List<String> seatNumbers) {
        Set<String> uniqueSeatNumbers = new HashSet<>();

        for (String seatNumber : seatNumbers) {
            if (!uniqueSeatNumbers.add(seatNumber)) {
                throw new DuplicatedSeatNumberException(seatNumber);
            }
        }
    }
}