package pl.exceptionhandled.reservationlab.reservation.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatAlreadyReservedException;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.user.AppUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatIsAvailableRuleTest {
    @Mock
    private ReservationRepository reservationRepository;

    private SeatIsAvailableRule rule;

    private AppUser user;
    private Event event;
    private Seat seat;

    @BeforeEach
    void setUp() {
        rule = new SeatIsAvailableRule(reservationRepository);

        user = createUser();
        event = createEvent();
        seat = createSeat(event);
    }

    @Test
    void shouldReturnTrueWhenSeatIsAvailable() {
        // arrange
        ReservationCreationContext context = new ReservationCreationContext(user, event, seat);

        // act
        when(reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                event.getId(),
                seat.getId(),
                ReservationStatus.ACTIVE_STATUSES)
        ).thenReturn(false);

        boolean result = rule.isSatisfiedBy(context);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSeatIsAlreadyReserved() {
        // arrange
        ReservationCreationContext context = new ReservationCreationContext(user, event, seat);

        //act
        when(reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                event.getId(),
                seat.getId(),
                ReservationStatus.ACTIVE_STATUSES)
        ).thenReturn(true);

        boolean result = rule.isSatisfiedBy(context);

        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateSeatIsAlreadyReservedException() {
        // arrange
        ReservationCreationContext context = new ReservationCreationContext(user, event, seat);

        // act
        RuntimeException exception = rule.exception(context);

        // assert
        assertThat(exception)
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    private AppUser createUser() {
        AppUser user = AppUser.builder()
                .email("email@domain.pl")
                .username("username")
                .build();

        user.setId(UUID.randomUUID());

        return user;
    }

    private Event createEvent() {
        Event event = Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .build();

        event.setId(UUID.randomUUID());

        return event;
    }

    private Seat createSeat(Event event) {
        Seat seat = Seat.builder()
                .seatNumber("A1")
                .event(event)
                .build();

        seat.setId(UUID.randomUUID());

        return seat;
    }
}
