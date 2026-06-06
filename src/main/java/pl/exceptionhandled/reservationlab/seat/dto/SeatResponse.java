package pl.exceptionhandled.reservationlab.seat.dto;

import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID eventId,
        String seatNumber
) {
}