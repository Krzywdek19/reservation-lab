package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class ReservationAlreadyCancelledException extends BusinessException {

    public ReservationAlreadyCancelledException(UUID reservationId) {
        super("Reservation is already cancelled: " + reservationId);
    }
}