package pl.exceptionhandled.reservationlab.event.service;

import java.time.Instant;

public record CreateEventCommand(String name, String location, Instant startsAt) {
}
