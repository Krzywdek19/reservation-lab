package pl.exceptionhandled.reservationlab.reservation;

import jakarta.persistence.*;
import lombok.*;
import pl.exceptionhandled.reservationlab.common.model.BaseEntity;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.user.AppUser;

import java.time.Instant;

@Entity
@Table(name = "reservations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Reservation extends BaseEntity {
    @Column(nullable = false, name = "reservation_number", updatable = false, insertable = false)
    private Long reservationNumber;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    @Column(name = "expires_at")
    private Instant expiresAt;
}
