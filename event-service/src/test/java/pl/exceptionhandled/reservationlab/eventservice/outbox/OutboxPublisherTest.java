package pl.exceptionhandled.reservationlab.eventservice.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.eventservice.event.message.EventCreatedPublisher;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private EventCreatedPublisher eventCreatedPublisher;

    @Mock
    private ObjectMapper objectMapper;

    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        outboxPublisher = new OutboxPublisher(
                outboxMessageRepository,
                eventCreatedPublisher,
                objectMapper
        );
    }

    @Test
    void shouldPublishPendingEventCreatedMessageAndMarkAsPublished() throws Exception {
        // given
        UUID eventId = UUID.randomUUID();
        String payload = """
                {
                  "eventId": "%s",
                  "name": "Java Meetup",
                  "location": "Warsaw",
                  "startsAt": "2026-07-01T18:00:00Z",
                  "seatNumbers": ["A1", "A2", "A3"]
                }
                """.formatted(eventId);

        OutboxMessage outboxMessage = OutboxMessage.builder()
                .aggregateId(eventId)
                .eventType(OutboxService.EVENT_CREATED)
                .payload(payload)
                .status(OutboxMessageStatus.PENDING)
                .retryCount(0)
                .build();

        EventCreatedMessage eventCreatedMessage = new EventCreatedMessage(
                eventId,
                "Java Meetup",
                "Warsaw",
                Instant.parse("2026-07-01T18:00:00Z"),
                List.of("A1", "A2", "A3")
        );

        when(outboxMessageRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxMessageStatus.PENDING))
                .thenReturn(List.of(outboxMessage));

        when(objectMapper.readValue(payload, EventCreatedMessage.class))
                .thenReturn(eventCreatedMessage);

        // when
        outboxPublisher.publishPendingMessages();

        // then
        verify(eventCreatedPublisher).publish(eventCreatedMessage);

        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxMessageStatus.PUBLISHED);
        assertThat(outboxMessage.getRetryCount()).isZero();
        assertThat(outboxMessage.getLastError()).isNull();
        assertThat(outboxMessage.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldMarkMessageAsFailedWhenPublishingFails() throws Exception {
        // given
        UUID eventId = UUID.randomUUID();
        String payload = "{}";

        OutboxMessage outboxMessage = OutboxMessage.builder()
                .aggregateId(eventId)
                .eventType(OutboxService.EVENT_CREATED)
                .payload(payload)
                .status(OutboxMessageStatus.PENDING)
                .retryCount(0)
                .build();

        EventCreatedMessage eventCreatedMessage = new EventCreatedMessage(
                eventId,
                "Java Meetup",
                "Warsaw",
                Instant.parse("2026-07-01T18:00:00Z"),
                List.of("A1", "A2")
        );

        when(outboxMessageRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxMessageStatus.PENDING))
                .thenReturn(List.of(outboxMessage));

        when(objectMapper.readValue(payload, EventCreatedMessage.class))
                .thenReturn(eventCreatedMessage);

        org.mockito.Mockito.doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(eventCreatedPublisher)
                .publish(eventCreatedMessage);

        // when
        outboxPublisher.publishPendingMessages();

        // then
        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxMessageStatus.PENDING);
        assertThat(outboxMessage.getRetryCount()).isEqualTo(1);
        assertThat(outboxMessage.getLastError()).isEqualTo("RabbitMQ unavailable");
        assertThat(outboxMessage.getPublishedAt()).isNull();
    }
}