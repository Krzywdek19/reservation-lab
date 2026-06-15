package pl.exceptionhandled.reservationlab.reservation.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.CannotConfirmCancelledReservationException;
import pl.exceptionhandled.reservationlab.reservation.exception.CannotConfirmExpiredReservationException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationAlreadyCancelledException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationAlreadyConfirmedException;
import pl.exceptionhandled.reservationlab.reservation.exception.ReservationNotFoundException;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatAlreadyReservedException;
import pl.exceptionhandled.reservationlab.reservation.rule.ReservationCreationContext;
import pl.exceptionhandled.reservationlab.reservation.rule.ReservationRule;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;
    private final List<ReservationRule> reservationRules;

    @Override
    public Reservation createReservation(@Valid CreateReservationCommand command) {
        var event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        var seat = seatRepository.findById(command.seatId())
                .orElseThrow(() -> new SeatNotFoundException(command.seatId()));

        var user = appUserRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        var context = new ReservationCreationContext(user,event,seat);

        for(ReservationRule rule : reservationRules) {
            if(!rule.isSatisfiedBy(context)) {
                throw rule.exception(context);
            }
        }

        var reservation = Reservation.builder()
                .event(event)
                .seat(seat)
                .user(user)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .build();

        try {
            var saved = reservationRepository.saveAndFlush(reservation);
            entityManager.refresh(saved);
            return saved;
        }catch (DataIntegrityViolationException exception) {
            throw new SeatAlreadyReservedException(seat.getId(), event.getId());
        }
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

        if(reservation.getStatus().equals(ReservationStatus.EXPIRED)) {
            throw new CannotConfirmExpiredReservationException(reservationId);
        }

        if(reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(Instant.now())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            throw new CannotConfirmExpiredReservationException(reservationId);
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