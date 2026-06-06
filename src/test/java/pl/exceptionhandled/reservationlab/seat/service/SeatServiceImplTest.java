package pl.exceptionhandled.reservationlab.seat.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatAlreadyExistsException;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    @Test
    void createSeatShouldCreateSeat() {
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .startsAt(Instant.now())
                .build();
        event.setId(eventId);

        var command = new CreateSeatCommand(eventId, "A1");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.existsByEvent_IdAndSeatNumber(eventId, "A1")).thenReturn(false);
        when(seatRepository.save(any(Seat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Seat result = seatService.createSeat(command);

        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getSeatNumber()).isEqualTo("A1");

        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void createSeatShouldThrowWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        var command = new CreateSeatCommand(eventId, "A1");

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.createSeat(command))
                .isInstanceOf(EventNotFoundException.class);

        verify(seatRepository, never()).save(any());
    }

    @Test
    void createSeatShouldThrowWhenSeatAlreadyExists() {
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .startsAt(Instant.now())
                .build();
        event.setId(eventId);

        var command = new CreateSeatCommand(eventId, "A1");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.existsByEvent_IdAndSeatNumber(eventId, "A1")).thenReturn(true);

        assertThatThrownBy(() -> seatService.createSeat(command))
                .isInstanceOf(SeatAlreadyExistsException.class);

        verify(seatRepository, never()).save(any());
    }

    @Test
    void getSeatShouldReturnSeat() {
        UUID seatId = UUID.randomUUID();

        Seat seat = Seat.builder()
                .seatNumber("A1")
                .build();
        seat.setId(seatId);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        Seat result = seatService.getSeat(seatId);

        assertThat(result).isEqualTo(seat);
    }

    @Test
    void getSeatShouldThrowWhenSeatNotFound() {
        UUID seatId = UUID.randomUUID();

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.getSeat(seatId))
                .isInstanceOf(SeatNotFoundException.class);
    }

    @Test
    void getEventSeatsShouldReturnSeats() {
        UUID eventId = UUID.randomUUID();

        Seat first = Seat.builder().seatNumber("A1").build();
        Seat second = Seat.builder().seatNumber("A2").build();

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(seatRepository.findByEvent_Id(eventId)).thenReturn(List.of(first, second));

        List<Seat> result = seatService.getEventSeats(eventId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(first, second);
    }

    @Test
    void getEventSeatsShouldThrowWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThatThrownBy(() -> seatService.getEventSeats(eventId))
                .isInstanceOf(EventNotFoundException.class);

        verify(seatRepository, never()).findByEvent_Id(any());
    }
}