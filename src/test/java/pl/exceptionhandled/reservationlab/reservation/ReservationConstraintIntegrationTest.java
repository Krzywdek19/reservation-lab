package pl.exceptionhandled.reservationlab.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationConstraintIntegrationTest {

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
    void shouldAllowNewReservationForSameSeatAfterCancellation() throws Exception {
        String userId = createUser("john@example.com", "john");
        String eventId = createEvent("Java Meetup", "Warsaw");
        String seatId = createSeat(eventId, "A1");

        String reservationId = createReservation(userId, eventId, seatId);

        mockMvc.perform(patch("/api/v1/reservations/{reservationId}/cancel", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

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
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(reservationRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldReturnConflictWhenActiveReservationAlreadyExistsForSeat() throws Exception {
        String userId = createUser("adam@example.com", "adam");
        String eventId = createEvent("Spring Meetup", "Krakow");
        String seatId = createSeat(eventId, "B1");

        createReservation(userId, eventId, seatId);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "eventId": "%s",
                                  "seatId": "%s"
                                }
                                """.formatted(userId, eventId, seatId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    private String createUser(String email, String username) throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "username": "%s"
                                }
                                """.formatted(email, username)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "id");
    }

    private String createEvent(String name, String location) throws Exception {
        String response = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "location": "%s",
                                  "startsAt": "%s"
                                }
                                """.formatted(name, location, Instant.now().plusSeconds(3600))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "id");
    }

    private String createSeat(String eventId, String seatNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/events/{eventId}/seats", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "seatNumber": "%s"
                                }
                                """.formatted(seatNumber)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "id");
    }

    private String createReservation(String userId, String eventId, String seatId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "eventId": "%s",
                                  "seatId": "%s"
                                }
                                """.formatted(userId, eventId, seatId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "id");
    }

    private String extractJsonValue(String json, String fieldName) {
        String field = "\"" + fieldName + "\":\"";
        int start = json.indexOf(field) + field.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}