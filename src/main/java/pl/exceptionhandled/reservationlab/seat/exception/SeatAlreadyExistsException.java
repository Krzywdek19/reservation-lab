package pl.exceptionhandled.reservationlab.seat.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

public class SeatAlreadyExistsException extends BusinessException {
    public SeatAlreadyExistsException(String eventName, String seatNumber) {
        super(String.format("Seat with number %s already exists for event %s", seatNumber, eventName));
    }
}
