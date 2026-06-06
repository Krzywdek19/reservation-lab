package pl.exceptionhandled.reservationlab.reservation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.reservation.dto.CreateReservationRequest;
import pl.exceptionhandled.reservationlab.reservation.dto.ReservationResponse;
import pl.exceptionhandled.reservationlab.reservation.mapper.ReservationMapper;
import pl.exceptionhandled.reservationlab.reservation.service.ReservationService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationResponse createReservation(CreateReservationRequest request) {
        var command = reservationMapper.toCommand(request);
        var reservation = reservationService.createReservation(command);
        return reservationMapper.toResponse(reservation);
    }

    public ReservationResponse getReservation(UUID reservationId) {
        var reservation = reservationService.getReservation(reservationId);
        return reservationMapper.toResponse(reservation);
    }

    public ReservationResponse confirmReservation(UUID reservationId) {
        var reservation = reservationService.confirmReservation(reservationId);
        return reservationMapper.toResponse(reservation);
    }

    public ReservationResponse cancelReservation(UUID reservationId) {
        var reservation = reservationService.cancelReservation(reservationId);
        return reservationMapper.toResponse(reservation);
    }
}