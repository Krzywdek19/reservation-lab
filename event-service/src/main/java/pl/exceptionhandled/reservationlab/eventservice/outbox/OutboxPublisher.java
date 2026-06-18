package pl.exceptionhandled.reservationlab.eventservice.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxMessageRepository outboxMessageRepository;
    private final EventCreatedPublisher eventCreatedPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pendingMessages = outboxMessageRepository
                .findTop50ByStatusOrderByCreatedAtAsc(OutboxMessageStatus.PENDING);

        for (OutboxMessage outboxMessage : pendingMessages) {
            publishSingleMessage(outboxMessage);
        }
    }

    private void publishSingleMessage(OutboxMessage outboxMessage) {
        try {
            if(OutboxService.EVENT_CREATED.equals(outboxMessage.getEventType())) {
                EventCreatedMessage message = objectMapper.readValue(
                        outboxMessage.getPayload(),
                        EventCreatedMessage.class
                );

                eventCreatedPublisher.publish(message);
                outboxMessage.markAsPublished();
                return;
            }
            outboxMessage.markAsFailed("Unsupported outbox event type: " + outboxMessage.getEventType());
        }catch (Exception exception) {
            outboxMessage.markAsFailed(exception.getMessage());
        }
    }
}
