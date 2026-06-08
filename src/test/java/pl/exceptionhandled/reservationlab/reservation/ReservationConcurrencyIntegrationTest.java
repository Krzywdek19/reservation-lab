package pl.exceptionhandled.reservationlab.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.support.ReservationLabApiClient;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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

    private ReservationLabApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new ReservationLabApiClient(mockMvc);
    }

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
        String eventId = apiClient.createEvent("Java Meetup", "Warsaw");
        String seatId = apiClient.createSeat(eventId, "A1");
        List<String> usersId = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            String userId = apiClient.createUser(String.format("john%s@example.com", i), "John");
            usersId.add(userId);
        }
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(20);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (String userId : usersId) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    int status = apiClient.tryCreateReservation(userId, eventId, seatId);

                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet();
                    } else {
                        errors.add(new IllegalStateException("Unexpected status: " + status));
                    }
                } catch (Throwable throwable) {
                    errors.add(throwable);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);

        executorService.shutdown();

        assertThat(finished).isTrue();
        assertThat(errors).isEmpty();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(19);

        assertThat(reservationRepository.findAll()
                .stream()
                .filter(reservation -> ReservationStatus.ACTIVE_STATUSES.contains(reservation.getStatus()))
                .toList()
        ).hasSize(1);
    }
}
