package pl.exceptionhandled.reservationlab.eventservice.event.message;

import java.time.Instant;
import java.util.UUID;

public record EventCreatedMessage(
        UUID eventId,
        String name,
        String location,
        Instant startsAt
) {
}
