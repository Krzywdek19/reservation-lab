package pl.exceptionhandled.reservationlab.seat;

import jakarta.persistence.*;
import lombok.*;
import pl.exceptionhandled.reservationlab.common.model.BaseEntity;
import pl.exceptionhandled.reservationlab.event.Event;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_seat_event_number",
                columnNames = {"event_id", "seat_number"}
        )
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Seat extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

}
