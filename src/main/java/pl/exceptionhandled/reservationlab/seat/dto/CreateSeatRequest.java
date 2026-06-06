package pl.exceptionhandled.reservationlab.seat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSeatRequest(
        @NotBlank String seatNumber
) {
}