package pl.exceptionhandled.reservationlab.event.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event createEvent(@Valid CreateEventCommand command) {
        Event event = Event.builder()
                .name(command.name())
                .location(command.location())
                .startsAt(command.startsAt())
                .build();

        return eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }
}