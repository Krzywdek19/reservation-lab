package pl.exceptionhandled.reservationlab.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.CannotConfirmCancelledReservationException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationAlreadyCancelledException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationAlreadyConfirmedException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatAlreadyReservedException;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatDoesNotBelongToEventException;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.util.List;
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
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        var seat = seatRepository.findById(command.seatId())
                .orElseThrow(() -> new SeatNotFoundException(command.seatId()));

        var user = appUserRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (!seat.getEvent().getId().equals(event.getId())) {
            throw new SeatDoesNotBelongToEventException(seat.getId(), event.getId());
        }

        boolean alreadyReserved = reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                event.getId(),
                seat.getId(),
                ReservationStatus.ACTIVE_STATUSES
        );

        if (alreadyReserved) {
            throw new SeatAlreadyReservedException(seat.getId(), event.getId());
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
    @Transactional(readOnly = true)
    public Reservation getReservation(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(UUID userId) {
        return reservationRepository.findByUser_Id(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getEventReservations(UUID eventId) {
        return reservationRepository.findByEvent_Id(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSeatAvailable(UUID eventId, UUID seatId) {
        return !reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                eventId,
                seatId,
                ReservationStatus.ACTIVE_STATUSES
        );
    }

    @Override
    public Reservation confirmReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
            throw new ReservationAlreadyConfirmedException(reservationId);
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new CannotConfirmCancelledReservationException(reservationId);
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservation;
    }

    @Override
    public Reservation cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new ReservationAlreadyCancelledException(reservationId);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return reservation;
    }
}