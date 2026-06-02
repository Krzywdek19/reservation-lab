CREATE INDEX idx_seats_event_id
    ON seats(event_id);

CREATE INDEX idx_reservations_user_id
    ON reservations(user_id);

CREATE INDEX idx_reservations_event_id
    ON reservations(event_id);

CREATE INDEX idx_reservations_event_status
    ON reservations(event_id, status);

CREATE UNIQUE INDEX uq_active_reservation_event_seat
    ON reservations(event_id, seat_id)
    WHERE status IN ('PENDING', 'CONFIRMED');