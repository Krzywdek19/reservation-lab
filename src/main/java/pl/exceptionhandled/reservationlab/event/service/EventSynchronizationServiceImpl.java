package pl.exceptionhandled.reservationlab.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;

@Service
@RequiredArgsConstructor
public class EventSynchronizationServiceImpl implements EventSynchronizationService {
    private final EventRepository eventRepository;

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

        eventRepository.save(event);
    }
}
