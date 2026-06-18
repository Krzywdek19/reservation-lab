package pl.exceptionhandled.reservationlab.eventservice.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.exceptionhandled.reservationlab.eventservice.seat.exception.DuplicatedSeatNumberException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicatedSeatNumberException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleDuplicatedSeatNumberException(DuplicatedSeatNumberException exception) {
        return Map.of("message", exception.getMessage());
    }
}
