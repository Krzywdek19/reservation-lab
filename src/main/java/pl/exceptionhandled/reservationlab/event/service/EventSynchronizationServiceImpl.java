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

    @Transactional
    @Override
    public void synchronizeEventCreate(EventCreatedMessage message) {
        if(eventRepository.existsById(message.eventId())) {
            return;
        }

        Event event = Event.builder()
                .name(message.name())
                .location(message.location())
                .startsAt(message.startsAt())
                .build();

        event.setId(message.eventId());

        eventRepository.save(event);
    }
}
