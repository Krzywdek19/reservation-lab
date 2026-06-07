package pl.exceptionhandled.reservationlab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationLabFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldCreateFullReservationFlow() throws Exception {
        String userResponse = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "username": "john"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = extractJsonValue(userResponse, "id");

        String eventResponse = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Java Meetup",
                                  "location": "Warsaw",
                                  "startsAt": "%s"
                                }
                                """.formatted(Instant.now().plusSeconds(3600))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = extractJsonValue(eventResponse, "id");

        String seatResponse = mockMvc.perform(post("/api/v1/events/{eventId}/seats", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "seatNumber": "A1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String seatId = extractJsonValue(seatResponse, "id");

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "eventId": "%s",
                                  "seatId": "%s"
                                }
                                """.formatted(userId, eventId, seatId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.seatId").value(seatId))
                .andExpect(jsonPath("$.reservationNumber").exists())
                .andExpect(jsonPath("$.reservationNumber").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(appUserRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(seatRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateSeatForSameEvent() throws Exception {
        String eventResponse = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spring Meetup",
                                  "location": "Krakow",
                                  "startsAt": "%s"
                                }
                                """.formatted(Instant.now().plusSeconds(3600))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventId = extractJsonValue(eventResponse, "id");

        String seatJson = """
                {
                  "seatNumber": "B1"
                }
                """;

        mockMvc.perform(post("/api/v1/events/{eventId}/seats", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seatJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/events/{eventId}/seats", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seatJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        assertThat(seatRepository.findAll()).hasSize(1);
    }

    private String extractJsonValue(String json, String fieldName) {
        String field = "\"" + fieldName + "\":\"";
        int start = json.indexOf(field) + field.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}