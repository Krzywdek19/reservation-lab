CREATE TABLE seats (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_seats_event
                   FOREIGN KEY (event_id)
                   REFERENCES events(id)
                   ON DELETE CASCADE,

    CONSTRAINT uq_seat_event_number
                   UNIQUE (event_id, seat_number)
);

CREATE INDEX idx_seats_event_id
on seats(event_id)