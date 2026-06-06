package pl.exceptionhandled.reservationlab.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateEventRequest(
        @NotBlank String name,
        @NotBlank String location,
        @NotNull Instant startsAt
) {
}