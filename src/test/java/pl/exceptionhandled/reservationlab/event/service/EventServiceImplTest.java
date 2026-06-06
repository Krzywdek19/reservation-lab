package pl.exceptionhandled.reservationlab.event.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void createEventShouldCreateEvent() {
        var command = new CreateEventCommand(
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600)
        );

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(command);

        assertThat(result.getName()).isEqualTo(command.name());
        assertThat(result.getLocation()).isEqualTo(command.location());
        assertThat(result.getStartsAt()).isEqualTo(command.startsAt());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEventShouldReturnEvent() {
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .startsAt(Instant.now())
                .build();
        event.setId(eventId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        Event result = eventService.getEvent(eventId);

        assertThat(result).isEqualTo(event);
    }

    @Test
    void getEventShouldThrowWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(eventId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void getEventsShouldReturnAllEvents() {
        Event first = Event.builder().name("Java").location("Warsaw").startsAt(Instant.now()).build();
        Event second = Event.builder().name("Spring").location("Krakow").startsAt(Instant.now()).build();

        when(eventRepository.findAll()).thenReturn(List.of(first, second));

        List<Event> result = eventService.getEvents();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(first, second);
    }
}