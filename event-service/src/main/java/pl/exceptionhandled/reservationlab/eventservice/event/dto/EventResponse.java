package pl.exceptionhandled.reservationlab.eventservice.event.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String location,
        Instant startsAt,
        Instant createdAt
) {
}