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
import pl.exceptionhandled.reservationlab.support.ReservationLabApiClient;
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

    private ReservationLabApiClient apiClient;

    @BeforeEach
    void setUp() {
        this.apiClient = new ReservationLabApiClient(mockMvc);
    }

    @BeforeEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldAllowNewReservationForSameSeatAfterCancellation() throws Exception {
        String userId = apiClient.createUser("john@example.com", "john");
        String eventId = apiClient.createEvent("Java Meetup", "Warsaw");
        String seatId = apiClient.createSeat(eventId, "A1");

        String reservationId = apiClient.createReservation(userId, eventId, seatId);

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
        String userId = apiClient.createUser("adam@example.com", "adam");
        String eventId = apiClient.createEvent("Spring Meetup", "Krakow");
        String seatId = apiClient.createSeat(eventId, "B1");

        apiClient.createReservation(userId, eventId, seatId);

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
}