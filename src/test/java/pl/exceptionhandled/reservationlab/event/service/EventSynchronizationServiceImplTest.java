
package pl.exceptionhandled.reservationlab.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSynchronizationServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    private EventSynchronizationServiceImpl eventSynchronizationService;

    @BeforeEach
    void setUp() {
        eventSynchronizationService = new EventSynchronizationServiceImpl(
                eventRepository,
                seatRepository
        );
    }

    @Test
    void shouldCreateEventFromEventCreatedMessage() {
        // arrange
        UUID externalEventId = UUID.randomUUID();
        Instant startsAt = Instant.now().plusSeconds(3600);

        EventCreatedMessage message = new EventCreatedMessage(
                externalEventId,
                "Java Meetup",
                "Warsaw",
                startsAt,
                List.of("A1", "A2")
        );

        when(eventRepository.existsByExternalEventId(externalEventId))
                .thenReturn(false);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // act
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getExternalEventId()).isEqualTo(externalEventId);
        assertThat(savedEvent.getName()).isEqualTo("Java Meetup");
        assertThat(savedEvent.getLocation()).isEqualTo("Warsaw");
        assertThat(savedEvent.getStartsAt()).isEqualTo(startsAt);
    }

    @Test
    void shouldNotCreateEventWhenEventAlreadyExists() {
        // arrange
        UUID externalEventId = UUID.randomUUID();

        EventCreatedMessage message = new EventCreatedMessage(
                externalEventId,
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600),
                List.of("A1", "A2")
        );

        when(eventRepository.existsByExternalEventId(externalEventId))
                .thenReturn(true);

        // act
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        verify(eventRepository, never()).save(any(Event.class));
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldCreateEventOnlyOnceWhenSameMessageIsProcessedTwice() {
        // arrange
        UUID externalEventId = UUID.randomUUID();

        EventCreatedMessage message = new EventCreatedMessage(
                externalEventId,
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600),
                List.of("A1", "A2", "A3")
        );

        when(eventRepository.existsByExternalEventId(externalEventId))
                .thenReturn(false)
                .thenReturn(true);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // act
        eventSynchronizationService.synchronizeEventCreated(message);
        eventSynchronizationService.synchronizeEventCreated(message);

        // assert
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateEventAndSeatsFromEventCreatedMessage() {
        // given
        UUID externalEventId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-07-01T18:00:00Z");

        EventCreatedMessage message = new EventCreatedMessage(
                externalEventId,
                "Java Meetup",
                "Warsaw",
                startsAt,
                List.of("A1", "A2", "A3")
        );

        when(eventRepository.existsByExternalEventId(externalEventId))
                .thenReturn(false);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        eventSynchronizationService.synchronizeEventCreated(message);

        // then
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getExternalEventId()).isEqualTo(externalEventId);
        assertThat(savedEvent.getName()).isEqualTo("Java Meetup");
        assertThat(savedEvent.getLocation()).isEqualTo("Warsaw");
        assertThat(savedEvent.getStartsAt()).isEqualTo(startsAt);

        ArgumentCaptor<List<Seat>> seatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(seatsCaptor.capture());

        List<Seat> savedSeats = seatsCaptor.getValue();

        assertThat(savedSeats)
                .extracting(Seat::getSeatNumber)
                .containsExactly("A1", "A2", "A3");

        assertThat(savedSeats)
                .allSatisfy(seat -> assertThat(seat.getEvent()).isEqualTo(savedEvent));
    }

    @Test
    void shouldNotCreateEventAndSeatsWhenEventAlreadyExists() {
        // given
        UUID externalEventId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-07-01T18:00:00Z");

        EventCreatedMessage message = new EventCreatedMessage(
                externalEventId,
                "Java Meetup",
                "Warsaw",
                startsAt,
                List.of("A1", "A2", "A3")
        );

        when(eventRepository.existsByExternalEventId(externalEventId))
                .thenReturn(true);

        // when
        eventSynchronizationService.synchronizeEventCreated(message);

        // then
        verify(eventRepository, never()).save(any(Event.class));
        verify(seatRepository, never()).saveAll(anyList());
    }
}

