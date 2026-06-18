package pl.exceptionhandled.reservationlab.eventservice.seat;

import jakarta.persistence.*;
import lombok.*;
import pl.exceptionhandled.reservationlab.eventservice.event.Event;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_seat_event_number",
                columnNames = {"event_id", "seat_number"}
        )
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if(id == null) {
            id = UUID.randomUUID();
        }

        if(createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
