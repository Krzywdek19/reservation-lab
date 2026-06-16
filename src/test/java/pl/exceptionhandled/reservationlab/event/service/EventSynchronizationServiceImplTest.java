package pl.exceptionhandled.reservationlab.event.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSynchronizationServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventSynchronizationServiceImpl eventSynchronizationService;

    @Test
    void shouldCreateEventFromEventCreatedMessage() {
        // arrange
        UUID eventId = UUID.randomUUID();
        Instant startsAt = Instant.now().plusSeconds(3600);

        EventCreatedMessage message = new EventCreatedMessage(
                eventId,
                "Java Meetup",
                "Warsaw",
                startsAt
        );

        when(eventRepository.existsById(eventId)).thenReturn(false);

        // act
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getId()).isEqualTo(eventId);
        assertThat(savedEvent.getName()).isEqualTo("Java Meetup");
        assertThat(savedEvent.getLocation()).isEqualTo("Warsaw");
        assertThat(savedEvent.getStartsAt()).isEqualTo(startsAt);
    }

    @Test
    void shouldNotCreateEventWhenEventAlreadyExists() {
        // arrange
        UUID eventId = UUID.randomUUID();

        EventCreatedMessage message = new EventCreatedMessage(
                eventId,
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600)
        );

        when(eventRepository.existsById(eventId)).thenReturn(true);

        // act
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldCreateEventOnlyOnceWhenSameMessageIsProcessedTwice() {
        // arrange
        UUID eventId = UUID.randomUUID();

        EventCreatedMessage message = new EventCreatedMessage(
                eventId,
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600)
        );

        when(eventRepository.existsById(eventId))
                .thenReturn(false)
                .thenReturn(true);

        // act
        eventSynchronizationService.synchronizeEventCreated(message);
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        verify(eventRepository, times(1)).save(any(Event.class));
    }
}