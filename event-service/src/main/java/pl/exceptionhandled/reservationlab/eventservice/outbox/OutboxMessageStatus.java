package pl.exceptionhandled.reservationlab.eventservice.outbox;

public enum OutboxMessageStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
