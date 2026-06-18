package pl.exceptionhandled.reservationlab.event.message;

import org.junit.jupiter.api.Test;
import pl.exceptionhandled.reservationlab.event.message.listener.EventCreatedMessageListener;
import pl.exceptionhandled.reservationlab.event.service.EventSynchronizationService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventCreatedMessageListenerTest {

    private final EventSynchronizationService eventSynchronizationService =
            mock(EventSynchronizationService.class);

    private final EventCreatedMessageListener listener =
            new EventCreatedMessageListener(eventSynchronizationService);

    @Test
    void shouldDelegateEventCreatedMessageToSynchronizationService() {
        EventCreatedMessage message = new EventCreatedMessage(
                UUID.randomUUID(),
                "Java Meetup",
                "Warsaw",
                Instant.now().plusSeconds(3600),
                List.of()
        );

        listener.handle(message);

        verify(eventSynchronizationService).synchronizeEventCreated(message);
    }
}
