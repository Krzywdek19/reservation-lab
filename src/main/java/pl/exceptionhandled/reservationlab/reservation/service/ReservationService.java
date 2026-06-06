package pl.exceptionhandled.reservationlab.reservation.service;

import pl.exceptionhandled.reservationlab.reservation.Reservation;

import java.util.List;
import java.util.UUID;

public interface ReservationService {

    Reservation createReservation(CreateReservationCommand command);

    Reservation confirmReservation(UUID reservationId);

    Reservation cancelReservation(UUID reservationId);

    Reservation getReservation(UUID reservationId);

    List<Reservation> getUserReservations(UUID userId);

    List<Reservation> getEventReservations(UUID eventId);

    boolean isSeatAvailable(UUID eventId, UUID seatId);
}