package pl.exceptionhandled.reservationlab.seat.service;

import java.util.UUID;

public record CreateSeatCommand(UUID eventId, String seatNumber) {
}
