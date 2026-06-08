package pl.exceptionhandled.reservationlab.support;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReservationLabApiClient {

    private final MockMvc mockMvc;

    public ReservationLabApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public String createUser(String email, String username) throws Exception {
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

    public String createEvent(String name, String location) throws Exception {
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

    public String createSeat(String eventId, String seatNumber) throws Exception {
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

    public String createReservation(String userId, String eventId, String seatId) throws Exception {
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

    public int tryCreateReservation(String userId, String eventId, String seatId) throws Exception {
        return mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userId": "%s",
                              "eventId": "%s",
                              "seatId": "%s"
                            }
                            """.formatted(userId, eventId, seatId)))
                .andReturn()
                .getResponse()
                .getStatus();
    }


    private String extractJsonValue(String json, String fieldName) {
        String field = "\"" + fieldName + "\":\"";
        int start = json.indexOf(field) + field.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}