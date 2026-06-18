package pl.exceptionhandled.reservationlab.eventservice.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {
    public static final String EVENT_CREATED = "EVENT_CREATED";

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public void saveEventCreatedMessage(EventCreatedMessage message) {
        OutboxMessage outboxMessage = OutboxMessage.builder()
                .aggregateId(message.eventId())
                .eventType(EVENT_CREATED)
                .payload(toJson(message))
                .status(OutboxMessageStatus.PENDING)
                .build();

        outboxMessageRepository.save(outboxMessage);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        }catch (Exception exception) {
            throw new IllegalStateException("Could not serialize outbox payload", exception);
        }
    }
}
