package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class CannotConfirmCancelledReservationException extends BusinessException {

    public CannotConfirmCancelledReservationException(UUID reservationId) {
        super("Cannot confirm cancelled reservation: " + reservationId);
    }
}