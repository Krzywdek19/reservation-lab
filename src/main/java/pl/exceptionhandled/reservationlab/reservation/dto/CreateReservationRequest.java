package pl.exceptionhandled.reservationlab.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(@NotNull UUID userId, @NotNull UUID eventId, @NotNull UUID seatId) {
}
