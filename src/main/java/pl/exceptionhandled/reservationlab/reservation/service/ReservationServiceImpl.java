package pl.exceptionhandled.reservationlab.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Reservation createReservation(CreateReservationCommand command) {
        var event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        var seat = seatRepository.findById(command.seatId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        var user = appUserRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!seat.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("Seat does not belong to the specified event");
        }

        boolean alreadyReserved = reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                event.getId(),
                seat.getId(),
                ReservationStatus.ACTIVE_STATUSES
        );

        if (alreadyReserved) {
            throw new IllegalStateException("Seat is already reserved for this event");
        }

        var reservation = Reservation.builder()
                .event(event)
                .seat(seat)
                .user(user)
                .status(ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation confirmReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
            throw new IllegalStateException("Reservation is already confirmed");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cancelled reservation cannot be confirmed");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservation;
    }

    @Override
    public Reservation cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return reservation;
    }
}