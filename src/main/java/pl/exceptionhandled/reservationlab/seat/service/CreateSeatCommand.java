package pl.exceptionhandled.reservationlab.seat.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSeatCommand(@NotNull UUID eventId, @NotBlank String seatNumber) {
}
