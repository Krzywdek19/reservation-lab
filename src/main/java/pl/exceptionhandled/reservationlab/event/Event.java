package pl.exceptionhandled.reservationlab.event;

import jakarta.persistence.*;
import lombok.*;
import pl.exceptionhandled.reservationlab.common.model.BaseEntity;
import pl.exceptionhandled.reservationlab.seat.Seat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "events")
public class Event extends BaseEntity {
    @Column(name = "external_event_id", unique = true)
    private UUID externalEventId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String location;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @Builder.Default
    @OneToMany(mappedBy = "event")
    private List<Seat> seats = new ArrayList<>();
}
