package pl.exceptionhandled.reservationlab.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventSynchronizationServiceImpl implements EventSynchronizationService {
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void synchronizeEventCreated(EventCreatedMessage message) {
        if (eventRepository.existsByExternalEventId(message.eventId())) {
            return;
        }

        Event event = Event.builder()
                .externalEventId(message.eventId())
                .name(message.name())
                .location(message.location())
                .startsAt(message.startsAt())
                .build();

        Event savedEvent = eventRepository.save(event);

        List<Seat> seats = message.seatNumbers()
                .stream()
                .distinct()
                .map(seatNumber -> Seat.builder()
                        .event(savedEvent)
                        .seatNumber(seatNumber)
                        .build())
                .toList();

        seatRepository.saveAll(seats);
    }
}
