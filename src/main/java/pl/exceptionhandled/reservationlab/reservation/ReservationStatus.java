package pl.exceptionhandled.reservationlab.reservation;

import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED;

    public static final Set<ReservationStatus> ACTIVE_STATUSES =
            EnumSet.of(PENDING, CONFIRMED);

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }
}