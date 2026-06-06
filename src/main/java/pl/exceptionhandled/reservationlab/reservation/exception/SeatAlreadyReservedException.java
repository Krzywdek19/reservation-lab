package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class SeatAlreadyReservedException extends BusinessException {

    public SeatAlreadyReservedException(UUID seatId, UUID eventId) {
        super("Seat " + seatId + " is already reserved for event " + eventId);
    }
}