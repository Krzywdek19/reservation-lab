package pl.exceptionhandled.reservationlab.reservation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.reservationlab.reservation.dto.CreateReservationRequest;
import pl.exceptionhandled.reservationlab.reservation.dto.ReservationResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return reservationFacade.createReservation(request);
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse getReservation(@PathVariable UUID reservationId) {
        return reservationFacade.getReservation(reservationId);
    }

    @PatchMapping("/{reservationId}/confirm")
    public ReservationResponse confirmReservation(@PathVariable UUID reservationId) {
        return reservationFacade.confirmReservation(reservationId);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(@PathVariable UUID reservationId) {
        return reservationFacade.cancelReservation(reservationId);
    }
}