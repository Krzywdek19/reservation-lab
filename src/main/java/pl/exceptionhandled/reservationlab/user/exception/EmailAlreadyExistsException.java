package pl.exceptionhandled.reservationlab.user.exception;

import pl.exceptionhandled.reservationlab.common.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' already exists", email));
    }
}
