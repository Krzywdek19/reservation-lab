package pl.exceptionhandled.reservationlab.event.message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventCreatedMessage (
        UUID eventId,
        String name,
        String location,
        Instant startsAt,
        List<String> seatNumbers
) {
}
