package pl.exceptionhandled.reservationlab.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUser_Id(UUID userId);

    boolean existsByEvent_IdAndSeat_IdAndStatusIn(
            UUID eventId,
            UUID seatId,
            Collection<ReservationStatus> statuses
    );
}