package pl.exceptionhandled.reservationlab.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.event.exception.EventNotFoundException;
import pl.exceptionhandled.reservationlab.seat.exception.SeatAlreadyExistsException;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.seat.exception.SeatNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    @Override
    public Seat createSeat(CreateSeatCommand command) {
        var event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        boolean alreadyExists = seatRepository.existsByEvent_IdAndSeatNumber(command.eventId(), command.seatNumber());
        if(alreadyExists) {
            throw new SeatAlreadyExistsException(event.getName(), command.seatNumber());
        }

        var seat = Seat.builder()
                .event(event)
                .seatNumber(command.seatNumber())
                .build();
        return seatRepository.save(seat);
    }

    @Override
    @Transactional(readOnly = true)
    public Seat getSeat(UUID seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getEventSeats(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        return seatRepository.findByEvent_Id(eventId);
    }
}
