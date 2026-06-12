package pl.exceptionhandled.reservationlab.reservation.rule;

import pl.exceptionhandled.reservationlab.reservation.exception.SeatDoesNotBelongToEventException;

public class SeatBelongsToEventRule implements ReservationRule{
    @Override
    public boolean isSatisfiedBy(ReservationCreationContext context) {
        return context.seat()
                .getEvent()
                .getId()
                .equals(context.event().getId());
    }

    @Override
    public RuntimeException exception(ReservationCreationContext context) {
        return new SeatDoesNotBelongToEventException(
                context.seat().getId(),
                context.event().getId()
        );
    }
}
