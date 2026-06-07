package pl.exceptionhandled.reservationlab.event.service;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateEventCommand(@NotBlank String name, @NotBlank String location, @NotNull @Future Instant startsAt) {
}
