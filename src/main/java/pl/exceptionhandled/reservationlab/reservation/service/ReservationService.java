package pl.exceptionhandled.reservationlab.reservation.service;

import pl.exceptionhandled.reservationlab.reservation.Reservation;

import java.util.UUID;

public interface ReservationService {

    Reservation createReservation(CreateReservationCommand command);

    Reservation confirmReservation(UUID reservationId);

    Reservation cancelReservation(UUID reservationId);
}