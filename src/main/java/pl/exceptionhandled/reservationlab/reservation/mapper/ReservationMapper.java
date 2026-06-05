package pl.exceptionhandled.reservationlab.reservation.mapper;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.reservation.Reservation;
import pl.exceptionhandled.reservationlab.reservation.dto.CreateReservationRequest;
import pl.exceptionhandled.reservationlab.reservation.dto.ReservationResponse;
import pl.exceptionhandled.reservationlab.reservation.service.CreateReservationCommand;

@Component
public class ReservationMapper {

    public CreateReservationCommand toCommand(CreateReservationRequest request) {
        return new CreateReservationCommand(
                request.userId(),
                request.eventId(),
                request.seatId()
        );
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getUser().getId(),
                reservation.getEvent().getId(),
                reservation.getSeat().getId(),
                reservation.getStatus()
        );
    }
}