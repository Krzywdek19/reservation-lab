package pl.exceptionhandled.reservationlab.reservation.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

import java.util.UUID;

public class SeatDoesNotBelongToEventException extends BusinessException {

    public SeatDoesNotBelongToEventException(UUID seatId, UUID eventId) {
        super("Seat " + seatId + " does not belong to event " + eventId);
    }
}