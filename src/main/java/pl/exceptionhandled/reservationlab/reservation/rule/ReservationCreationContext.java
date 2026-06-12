package pl.exceptionhandled.reservationlab.reservation.rule;

import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.user.AppUser;

public record ReservationCreationContext(
        AppUser appUser,
        Event event,
        Seat seat
) {
}
