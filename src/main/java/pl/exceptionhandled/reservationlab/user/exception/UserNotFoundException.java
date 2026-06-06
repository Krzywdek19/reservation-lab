package pl.exceptionhandled.reservationlab.user.exception;

import pl.exceptionhandled.reservationlab.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}