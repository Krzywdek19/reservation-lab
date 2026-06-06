package pl.exceptionhandled.reservationlab.common.exception;

public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }
}