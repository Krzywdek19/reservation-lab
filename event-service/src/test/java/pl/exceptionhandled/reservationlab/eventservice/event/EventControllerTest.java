package pl.exceptionhandled.reservationlab.eventservice.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.exceptionhandled.reservationlab.eventservice.common.exception.GlobalExceptionHandler;
import pl.exceptionhandled.reservationlab.eventservice.seat.exception.DuplicatedSeatNumberException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void shouldReturnBadRequestWhenSeatNumbersContainDuplicates() throws Exception {
        // given
        String requestBody = """
                {
                  "name": "Java Meetup",
                  "location": "Warsaw",
                  "startsAt": "2026-07-01T18:00:00Z",
                  "seatNumbers": ["A1", "A1", "A2"]
                }
                """;

        when(eventService.createEvent(any()))
                .thenThrow(new DuplicatedSeatNumberException("A1"));

        // when & then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicated seat number: A1"));
    }
}