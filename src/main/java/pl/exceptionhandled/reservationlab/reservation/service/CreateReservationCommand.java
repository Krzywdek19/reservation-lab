package pl.exceptionhandled.reservationlab.reservation.service;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationCommand(@NotNull UUID userId, @NotNull UUID eventId, @NotNull UUID seatId) {
}
