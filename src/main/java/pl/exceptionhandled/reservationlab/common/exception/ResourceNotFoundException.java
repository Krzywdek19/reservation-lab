package pl.exceptionhandled.reservationlab.common.exception;

public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}