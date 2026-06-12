package pl.exceptionhandled.reservationlab.reservation.rule;

public interface ReservationRule {
    boolean isSatisfiedBy(ReservationCreationContext context);
    RuntimeException exception(ReservationCreationContext context);
}
