package pl.exceptionhandled.reservationlab.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.*;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private UUID userId;
    private UUID eventId;
    private UUID seatId;
    private UUID reservationId;

    private AppUser user;
    private Event event;
    private Seat seat;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        user = AppUser.builder()
                .email("john@example.com")
                .username("john")
                .build();
        user.setId(userId);

        event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .build();
        event.setId(eventId);

        seat = Seat.builder()
                .event(event)
                .seatNumber("A1")
                .build();
        seat.setId(seatId);
    }

    @Test
    void createReservationShouldCreatePendingReservation() {
        CreateReservationCommand command = new CreateReservationCommand(userId,eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(eventId, seatId, ReservationStatus.ACTIVE_STATUSES)).thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(command);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());

        Reservation savedReservation = captor.getValue();
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void createReservationShouldThrowWhenEventNotFound() {
        CreateReservationCommand command = new CreateReservationCommand(userId, eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(seatRepository, appUserRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationShouldThrowWhenSeatNotFound() {
        CreateReservationCommand command = new CreateReservationCommand(userId, eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(SeatNotFoundException.class);

        verifyNoInteractions(appUserRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationShouldThrowWhenUserNotFound() {
        CreateReservationCommand command = new CreateReservationCommand(userId, eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationShouldThrowWhenSeatDoesNotBelongToEvent() {
        UUID otherEventId = UUID.randomUUID();

        Event otherEvent = Event.builder()
                .name("Other Event")
                .location("Krakow")
                .build();
        otherEvent.setId(otherEventId);

        Seat seatFromOtherEvent = Seat.builder()
                .event(otherEvent)
                .seatNumber("B1")
                .build();
        seatFromOtherEvent.setId(seatId);

        CreateReservationCommand command = new CreateReservationCommand(userId, eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seatFromOtherEvent));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(SeatDoesNotBelongToEventException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationShouldThrowWhenSeatAlreadyReserved() {
        CreateReservationCommand command = new CreateReservationCommand(userId, eventId, seatId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                eventId,
                seatId,
                ReservationStatus.ACTIVE_STATUSES
        )).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(SeatAlreadyReservedException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void confirmReservationShouldConfirmPendingReservation() {
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.confirmReservation(reservationId);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmReservationShouldThrowWhenReservationAlreadyConfirmed() {
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.CONFIRMED)
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
                .isInstanceOf(ReservationAlreadyConfirmedException.class);
    }

    @Test
    void confirmReservationShouldThrowWhenCancelledReservationIsConfirmed() {
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.CANCELLED)
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
                .isInstanceOf(CannotConfirmCancelledReservationException.class);
    }

    @Test
    void cancelReservationShouldCancelReservation() {
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.cancelReservation(reservationId);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelReservationShouldThrowWhenReservationAlreadyCancelled() {
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.CANCELLED)
                .build();
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId))
                .isInstanceOf(ReservationAlreadyCancelledException.class);
    }
}
