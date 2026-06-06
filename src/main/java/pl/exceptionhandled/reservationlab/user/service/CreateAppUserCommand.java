package pl.exceptionhandled.reservationlab.user.service;

public record CreateAppUserCommand(
        String email,
        String username
) {
}