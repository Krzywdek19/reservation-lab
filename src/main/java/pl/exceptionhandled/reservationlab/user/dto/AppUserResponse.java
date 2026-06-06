package pl.exceptionhandled.reservationlab.user.dto;

import java.time.Instant;
import java.util.UUID;

public record AppUserResponse(
        UUID id,
        String email,
        String username,
        Instant createdAt
) {
}