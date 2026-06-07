package pl.exceptionhandled.reservationlab.user.service;

import jakarta.validation.constraints.NotBlank;

public record CreateAppUserCommand(
        @NotBlank String email,
        @NotBlank String username
) {
}