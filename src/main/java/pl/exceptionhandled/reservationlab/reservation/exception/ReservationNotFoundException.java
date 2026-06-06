package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ReservationNotFoundException extends ResourceNotFoundException {

    public ReservationNotFoundException(UUID reservationId) {
        super("Reservation not found: " + reservationId);
    }
}