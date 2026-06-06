package pl.exceptionhandled.reservationlab.seat.exception;

import pl.exceptionhandled.reservationlab.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class SeatNotFoundException extends ResourceNotFoundException {

    public SeatNotFoundException(UUID seatId) {
        super("Seat not found: " + seatId);
    }
}