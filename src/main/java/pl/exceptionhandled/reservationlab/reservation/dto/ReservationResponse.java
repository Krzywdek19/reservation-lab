package pl.exceptionhandled.reservationlab.reservation.dto;

import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;

import java.util.UUID;

public record ReservationResponse(
        UUID id,
        Long reservationNumber,
        UUID userId,
        UUID eventId,
        UUID seatId,
        ReservationStatus status
) {
}