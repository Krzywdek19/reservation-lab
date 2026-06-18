package pl.exceptionhandled.reservationlab.eventservice.seat.exception;

public class DuplicatedSeatNumberException extends RuntimeException {

    public DuplicatedSeatNumberException(String seatNumber) {
        super("Duplicated seat number: " + seatNumber);
    }
}