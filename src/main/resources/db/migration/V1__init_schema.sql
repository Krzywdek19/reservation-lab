CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    username varchar(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
                        id UUID PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        location VARCHAR(255) NOT NULL,
                        starts_at TIMESTAMP NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seats (
                       id UUID PRIMARY KEY,
                       event_id UUID NOT NULL,
                       seat_number VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_seats_event
                           FOREIGN KEY (event_id)
                               REFERENCES events(id),

                       CONSTRAINT uq_seat_event_number
                           UNIQUE (event_id, seat_number)
);

CREATE SEQUENCE reservation_number_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE reservations (
                              id UUID PRIMARY KEY,
                              reservation_number BIGINT NOT NULL DEFAULT nextval('reservation_number_seq'),
                              user_id UUID NOT NULL,
                              event_id UUID NOT NULL,
                              seat_id UUID NOT NULL,
                              status VARCHAR(50) NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_reservations_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES app_users(id),

                              CONSTRAINT fk_reservations_event
                                  FOREIGN KEY (event_id)
                                      REFERENCES events(id),

                              CONSTRAINT fk_reservations_seat
                                  FOREIGN KEY (seat_id)
                                      REFERENCES seats(id)
);