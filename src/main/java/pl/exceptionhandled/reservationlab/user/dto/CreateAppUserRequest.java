package pl.exceptionhandled.reservationlab.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAppUserRequest(
        @NotBlank @Email String email,
        @NotBlank String username
) {
}