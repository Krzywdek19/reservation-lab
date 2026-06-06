package pl.exceptionhandled.reservationlab.seat.service;

import pl.exceptionhandled.reservationlab.seat.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatService {

    Seat createSeat(CreateSeatCommand command);

    Seat getSeat(UUID seatId);

    List<Seat> getEventSeats(UUID eventId);
}