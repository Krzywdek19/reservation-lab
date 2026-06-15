package pl.exceptionhandled.reservationlab.reservation.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.*;
import pl.exceptionhandled.reservationlab.reservation.rule.ReservationCreationContext;
import pl.exceptionhandled.reservationlab.reservation.rule.ReservationRule;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ReservationRule reservationRule;

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

        user = createUser(userId);
        event = createEvent(eventId);
        seat = createSeat(seatId, event);

        reservationService = new ReservationServiceImpl(
                reservationRepository,
                eventRepository,
                seatRepository,
                appUserRepository,
                entityManager,
                List.of(reservationRule)
        );
    }

    @Test
    void createReservationShouldCreatePendingReservationWithExpirationTime() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        givenExistingReservationResources();
        givenReservationRulesPass();
        givenSavedReservationIsReturned();

        // act
        Reservation result = reservationService.createReservation(command);

        // assert
        Reservation savedReservation = captureSavedReservation();

        assertThat(savedReservation.getUser()).isEqualTo(user);
        assertThat(savedReservation.getEvent()).isEqualTo(event);
        assertThat(savedReservation.getSeat()).isEqualTo(seat);
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(savedReservation.getExpiresAt()).isNotNull();
        assertThat(savedReservation.getExpiresAt()).isAfter(Instant.now().plusSeconds(60));

        assertThat(result).isSameAs(savedReservation);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);

        verify(entityManager).refresh(savedReservation);
    }

    @Test
    void createReservationShouldThrowWhenEventNotFound() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(seatRepository);
        verifyNoInteractions(appUserRepository);
        verifyNoInteractions(reservationRule);
        verifyNoInteractions(reservationRepository);
        verifyNoInteractions(entityManager);
    }

    @Test
    void createReservationShouldThrowWhenSeatNotFound() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(SeatNotFoundException.class);

        verifyNoInteractions(appUserRepository);
        verifyNoInteractions(reservationRule);
        verifyNoInteractions(reservationRepository);
        verifyNoInteractions(entityManager);
    }

    @Test
    void createReservationShouldThrowWhenUserNotFound() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(reservationRule);
        verifyNoInteractions(reservationRepository);
        verifyNoInteractions(entityManager);
    }

    @Test
    void createReservationShouldThrowWhenReservationRuleIsNotSatisfied() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        RuntimeException ruleException = new SeatAlreadyReservedException(seatId, eventId);

        givenExistingReservationResources();

        when(reservationRule.isSatisfiedBy(any(ReservationCreationContext.class)))
                .thenReturn(false);

        when(reservationRule.exception(any(ReservationCreationContext.class)))
                .thenReturn(ruleException);

        // act + assert
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isSameAs(ruleException);

        verify(reservationRepository, never()).saveAndFlush(any(Reservation.class));
        verifyNoInteractions(entityManager);
    }

    @Test
    void createReservationShouldTranslateDatabaseConflictToSeatAlreadyReservedException() {
        // arrange
        CreateReservationCommand command = createReservationCommand();

        givenExistingReservationResources();
        givenReservationRulesPass();

        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // act + assert
        assertThatThrownBy(() -> reservationService.createReservation(command))
                .isInstanceOf(SeatAlreadyReservedException.class);

        verify(reservationRepository).saveAndFlush(any(Reservation.class));
        verify(entityManager, never()).refresh(any());
    }

    @Test
    void confirmReservationShouldConfirmPendingReservation() {
        // arrange
        Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // act
        Reservation result = reservationService.confirmReservation(reservationId);

        // assert
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmReservationShouldThrowWhenReservationAlreadyConfirmed() {
        // arrange
        Reservation reservation = createReservationWithStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // act + assert
        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
                .isInstanceOf(ReservationAlreadyConfirmedException.class);
    }

    @Test
    void confirmReservationShouldThrowWhenCancelledReservationIsConfirmed() {
        // arrange
        Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // act + assert
        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
                .isInstanceOf(CannotConfirmCancelledReservationException.class);
    }

    @Test
    void cancelReservationShouldCancelPendingReservation() {
        // arrange
        Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // act
        Reservation result = reservationService.cancelReservation(reservationId);

        // assert
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelReservationShouldThrowWhenReservationAlreadyCancelled() {
        // arrange
        Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // act + assert
        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId))
                .isInstanceOf(ReservationAlreadyCancelledException.class);
    }

    @Test
    void confirmReservationShouldThrowWhenReservationIsExpired() {
        // arrange
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(60))
                .build();

        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
                .isInstanceOf(CannotConfirmExpiredReservationException.class);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    private CreateReservationCommand createReservationCommand() {
        return new CreateReservationCommand(userId, eventId, seatId);
    }

    private void givenExistingReservationResources() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void givenReservationRulesPass() {
        when(reservationRule.isSatisfiedBy(any(ReservationCreationContext.class)))
                .thenReturn(true);
    }

    private void givenSavedReservationIsReturned() {
        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Reservation captureSavedReservation() {
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        verify(reservationRepository).saveAndFlush(reservationCaptor.capture());

        return reservationCaptor.getValue();
    }

    private AppUser createUser(UUID id) {
        AppUser user = AppUser.builder()
                .email("john@example.com")
                .username("john")
                .build();

        user.setId(id);

        return user;
    }

    private Event createEvent(UUID id) {
        Event event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .startsAt(Instant.now().plusSeconds(3600))
                .build();

        event.setId(id);

        return event;
    }

    private Seat createSeat(UUID id, Event event) {
        Seat seat = Seat.builder()
                .event(event)
                .seatNumber("A1")
                .build();

        seat.setId(id);

        return seat;
    }

    private Reservation createReservationWithStatus(ReservationStatus status) {
        Reservation reservation = Reservation.builder()
                .status(status)
                .build();

        reservation.setId(reservationId);

        return reservation;
    }
}