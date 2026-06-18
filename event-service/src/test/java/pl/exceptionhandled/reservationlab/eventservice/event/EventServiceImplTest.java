package pl.exceptionhandled.reservationlab.eventservice.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.eventservice.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxService;
import pl.exceptionhandled.reservationlab.eventservice.seat.Seat;
import pl.exceptionhandled.reservationlab.eventservice.seat.exception.DuplicatedSeatNumberException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private OutboxService outboxService;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(
                eventRepository,
                outboxService
        );
    }

    @Test
    void shouldThrowExceptionWhenSeatNumbersContainsDuplicate() {
        // given
        CreateEventRequest request = new CreateEventRequest(
                "Java Meetup",
                "Warsaw",
                Instant.parse("2026-07-01T18:00:00Z"),
                List.of("A1", "A1", "A2")
        );

        // when & then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(DuplicatedSeatNumberException.class)
                .hasMessage("Duplicated seat number: A1");

        verify(eventRepository, never()).saveAndFlush(any(Event.class));
        verify(outboxService, never()).saveEventCreatedMessage(any());
    }

    @Test
    void shouldCreateEventWithSeatsAndStoreMessageInOutbox() {
        // given
        CreateEventRequest request = new CreateEventRequest(
                "Java Meetup",
                "Warsaw",
                Instant.parse("2026-07-01T18:00:00Z"),
                List.of("A1", "A2", "A3")
        );

        when(eventRepository.saveAndFlush(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        eventService.createEvent(request);

        // then
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).saveAndFlush(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getName()).isEqualTo("Java Meetup");
        assertThat(savedEvent.getLocation()).isEqualTo("Warsaw");
        assertThat(savedEvent.getStartsAt()).isEqualTo(Instant.parse("2026-07-01T18:00:00Z"));

        assertThat(savedEvent.getSeats())
                .extracting(Seat::getSeatNumber)
                .containsExactly("A1", "A2", "A3");

        assertThat(savedEvent.getSeats())
                .allSatisfy(seat -> assertThat(seat.getEvent()).isEqualTo(savedEvent));

        verify(outboxService).saveEventCreatedMessage(any());
    }
}
