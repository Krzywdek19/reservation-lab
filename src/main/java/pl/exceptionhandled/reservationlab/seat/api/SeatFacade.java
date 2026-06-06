package pl.exceptionhandled.reservationlab.seat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.seat.dto.CreateSeatRequest;
import pl.exceptionhandled.reservationlab.seat.dto.SeatResponse;
import pl.exceptionhandled.reservationlab.seat.mapper.SeatMapper;
import pl.exceptionhandled.reservationlab.seat.service.SeatService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatFacade {
    private final SeatService seatService;
    private final SeatMapper seatMapper;

    public SeatResponse createSeat(CreateSeatRequest request, UUID eventId) {
        var command = seatMapper.toCommand(request, eventId);
        return seatMapper.toResponse(seatService.createSeat(command));
    }

    public SeatResponse getSeat(UUID seatId) {
        return seatMapper.toResponse(seatService.getSeat(seatId));
    }

    public List<SeatResponse> getEventSeats(UUID eventId) {
        return seatMapper.toResponseList(seatService.getEventSeats(eventId));
    }
}
