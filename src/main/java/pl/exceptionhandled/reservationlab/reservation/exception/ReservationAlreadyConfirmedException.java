package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class ReservationAlreadyConfirmedException extends BusinessException {

    public ReservationAlreadyConfirmedException(UUID reservationId) {
        super("Reservation is already confirmed: " + reservationId);
    }
}