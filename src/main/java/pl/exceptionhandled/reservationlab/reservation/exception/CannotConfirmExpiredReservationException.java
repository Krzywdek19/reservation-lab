package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class CannotConfirmExpiredReservationException extends BusinessException {
    public CannotConfirmExpiredReservationException(UUID reservationId) {
        super("Cannot confirm expired reservation with id: " + reservationId);
    }
}
