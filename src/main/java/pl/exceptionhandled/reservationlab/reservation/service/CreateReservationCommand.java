package pl.exceptionhandled.reservationlab.reservation.service;

import java.util.UUID;

public record CreateReservationCommand(UUID userId, UUID eventId, UUID seatId) {
}
