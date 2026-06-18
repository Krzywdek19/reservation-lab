package pl.exceptionhandled.reservationlab.eventservice.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxMessage;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxMessageRepository;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxMessageStatus;
import pl.exceptionhandled.reservationlab.eventservice.outbox.OutboxService;
import pl.exceptionhandled.reservationlab.eventservice.seat.Seat;
import pl.exceptionhandled.reservationlab.eventservice.seat.SeatRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "app.outbox.publisher.enabled=false")
@AutoConfigureMockMvc
class EventCreationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @AfterEach
    void cleanUp() {
        outboxMessageRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void shouldCreateEventSeatsAndOutboxMessage() throws Exception {
        // given
        String requestBody = """
                {
                  "name": "Java Meetup",
                  "location": "Warsaw",
                  "startsAt": "2026-07-01T18:00:00Z",
                  "seatNumbers": ["A1", "A2", "A3"]
                }
                """;

        // when
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // then
        List<Event> events = eventRepository.findAll();

        assertThat(events).hasSize(1);

        Event savedEvent = events.get(0);

        assertThat(savedEvent.getName()).isEqualTo("Java Meetup");
        assertThat(savedEvent.getLocation()).isEqualTo("Warsaw");
        assertThat(savedEvent.getStartsAt()).isEqualTo(Instant.parse("2026-07-01T18:00:00Z"));

        List<Seat> seats = seatRepository.findByEvent_Id(savedEvent.getId());

        assertThat(seats)
                .extracting(Seat::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2", "A3");

        List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();

        assertThat(outboxMessages).hasSize(1);

        OutboxMessage outboxMessage = outboxMessages.get(0);

        assertThat(outboxMessage.getAggregateId()).isEqualTo(savedEvent.getId());
        assertThat(outboxMessage.getEventType()).isEqualTo(OutboxService.EVENT_CREATED);
        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxMessageStatus.PENDING);
        assertThat(outboxMessage.getRetryCount()).isZero();
        assertThat(outboxMessage.getPublishedAt()).isNull();

        assertThat(outboxMessage.getPayload()).contains("Java Meetup");
        assertThat(outboxMessage.getPayload()).contains("Warsaw");
        assertThat(outboxMessage.getPayload()).contains("A1");
        assertThat(outboxMessage.getPayload()).contains("A2");
        assertThat(outboxMessage.getPayload()).contains("A3");
    }
}