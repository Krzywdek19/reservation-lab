ALTER TABLE events
    ADD COLUMN external_event_id UUID;

CREATE UNIQUE INDEX uq_events_external_event_id
    ON events(external_event_id);