package pl.exceptionhandled.reservationlab.seat.mapper;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.dto.CreateSeatRequest;
import pl.exceptionhandled.reservationlab.seat.dto.SeatResponse;
import pl.exceptionhandled.reservationlab.seat.service.CreateSeatCommand;

import java.util.List;
import java.util.UUID;

@Component
public class SeatMapper {
    public CreateSeatCommand toCommand(CreateSeatRequest request, UUID eventId) {
        return new CreateSeatCommand(eventId, request.seatNumber());
    }

    public SeatResponse toResponse(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getEvent().getId(), seat.getSeatNumber());
    }

    public List<SeatResponse> toResponseList(List<Seat> seats){
        return seats.stream().map(this::toResponse).toList();
    }
}
