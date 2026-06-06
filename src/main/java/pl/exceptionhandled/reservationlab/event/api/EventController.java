package pl.exceptionhandled.reservationlab.event.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.reservationlab.event.dto.CreateEventRequest;
import pl.exceptionhandled.reservationlab.event.dto.EventResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventFacade eventFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventFacade.createEvent(request);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable UUID eventId) {
        return eventFacade.getEvent(eventId);
    }

    @GetMapping
    public List<EventResponse> getEvents() {
        return eventFacade.getEvents();
    }
}