package pl.exceptionhandled.reservationlab.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationConcurrencyIntegrationTest {
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
    void shouldAllowOnlyOneReservationWhenManyUsersReserveSameSeatConcurrently() throws Exception {
        try (ExecutorService executorService = Executors.newFixedThreadPool(20)) {
            shouldAllowOnlyOneReservationWithExecutor(executorService);
        }
    }

    @Test
    void shouldAllowOnlyOneReservationWhenManyUsersReserveSameSeatConcurrentlyWithVirtualThreads() throws Exception{
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            shouldAllowOnlyOneReservationWithExecutor(executorService);
        }
    }

    private void shouldAllowOnlyOneReservationWithExecutor(ExecutorService executorService) throws Exception {
        String eventId = createEvent("Java Meetup", "Warsaw");
        String seatId = createSeat(eventId, "A1");
        List<String> usersId = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            String userId = createUser(String.format("john%s@example.com", i), "John");
            usersId.add(userId);
        }
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(20);

        AtomicInteger successCount = new AtomicInteger();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (String userId : usersId) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    createReservation(userId, eventId, seatId);
                    successCount.incrementAndGet();
                } catch (Exception exception) {
                    exceptions.add(exception);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);

        executorService.shutdown();

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(1);

        assertThat(reservationRepository.findAll()
                .stream()
                .filter(reservation -> ReservationStatus.ACTIVE_STATUSES.contains(reservation.getStatus()))
                .toList()
        ).hasSize(1);
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
