package pl.exceptionhandled.reservationlab.reservation.api;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.event.Event;
import pl.exceptionhandled.reservationlab.event.EventRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationRepository;
import pl.exceptionhandled.reservationlab.reservation.ReservationStatus;
import pl.exceptionhandled.reservationlab.reservation.dto.CreateReservationRequest;
import pl.exceptionhandled.reservationlab.seat.Seat;
import pl.exceptionhandled.reservationlab.seat.SeatRepository;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createReservationShouldReturnCreated() throws Exception {
        AppUser user = appUserRepository.save(AppUser.builder()
                .email("john@example.com")
                .username("john")
                .build());

        Event event = eventRepository.save(Event.builder()
                .name("Java Meetup")
                .location("Warsaw")
                .startsAt(Instant.now().plusSeconds(3600))
                .build());

        Seat seat = seatRepository.save(Seat.builder()
                .event(event)
                .seatNumber("A1")
                .build());

        CreateReservationRequest request = new CreateReservationRequest(
                user.getId(),
                event.getId(),
                seat.getId()
        );

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.eventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.seatId").value(seat.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findAll().getFirst().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void createReservationShouldReturnConflictWhenSeatIsAlreadyReserved() throws Exception {
        AppUser user = appUserRepository.save(AppUser.builder()
                .email("adam@example.com")
                .username("adam")
                .build());

        Event event = eventRepository.save(Event.builder()
                .name("Spring Boot Meetup")
                .location("Krakow")
                .startsAt(Instant.now().plusSeconds(3600))
                .build());

        Seat seat = seatRepository.save(Seat.builder()
                .event(event)
                .seatNumber("B1")
                .build());

        CreateReservationRequest request = new CreateReservationRequest(
                user.getId(),
                event.getId(),
                seat.getId()
        );

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        assertThat(reservationRepository.findAll()).hasSize(1);
    }
}
