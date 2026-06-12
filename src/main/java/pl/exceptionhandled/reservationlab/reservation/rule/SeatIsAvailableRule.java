package pl.exceptionhandled.reservationlab.reservation.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.exception.SeatAlreadyReservedException;

@Component
@RequiredArgsConstructor
public class SeatIsAvailableRule implements ReservationRule {
    private final ReservationRepository reservationRepository;

    @Override
    public boolean isSatisfiedBy(ReservationCreationContext context) {
        return !reservationRepository.existsByEvent_IdAndSeat_IdAndStatusIn(
                context.event().getId(),
                context.seat().getId(),
                ReservationStatus.ACTIVE_STATUSES
        );
    }

    @Override
    public RuntimeException exception(ReservationCreationContext context) {
        return new SeatAlreadyReservedException(
                context.seat().getId(),
                context.event().getId()
        );
    }
}
