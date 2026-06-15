package pl.exceptionhandled.reservationlab.reservation.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatDoesNotBelongToEventException;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.user.AppUser;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeatBelongsToEventRuleTest {

    private SeatBelongsToEventRule rule;

    private AppUser user;
    private Event event;
    private Seat seat;

    @BeforeEach
    void setUp() {
        rule = new SeatBelongsToEventRule();

        user = createUser();
        event = createEvent();
        seat = createSeat(event);
    }

    @Test
    void shouldReturnTrueWhenSeatBelongsToEvent() {
        // arrange
        ReservationCreationContext context = new ReservationCreationContext(user, event, seat);

        // act
        boolean result = rule.isSatisfiedBy(context);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSeatDoesNotBelongToEvent() {
        // arrange
        Event otherEvent = createEvent();
        Seat seatFromOtherEvent = createSeat(otherEvent);

        ReservationCreationContext context = new ReservationCreationContext(
                user,
                event,
                seatFromOtherEvent
        );

        // act
        boolean result = rule.isSatisfiedBy(context);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateSeatDoesNotBelongToEventException() {
        // arrange
        ReservationCreationContext context = new ReservationCreationContext(user, event, seat);

        // act
        RuntimeException exception = rule.exception(context);

        // assert
        assertThat(exception)
                .isInstanceOf(SeatDoesNotBelongToEventException.class);
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