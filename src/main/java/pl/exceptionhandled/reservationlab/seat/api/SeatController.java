package pl.exceptionhandled.reservationlab.seat.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.reservationlab.seat.dto.CreateSeatRequest;
import pl.exceptionhandled.reservationlab.seat.dto.SeatResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatFacade seatFacade;

    @PostMapping("/api/v1/events/{eventId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatResponse createSeat(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateSeatRequest request
    ) {
        return seatFacade.createSeat(request, eventId);
    }

    @GetMapping("/api/v1/seats/{seatId}")
    public SeatResponse getSeat(@PathVariable UUID seatId) {
        return seatFacade.getSeat(seatId);
    }

    @GetMapping("/api/v1/events/{eventId}/seats")
    public List<SeatResponse> getEventSeats(@PathVariable UUID eventId) {
        return seatFacade.getEventSeats(eventId);
    }
}