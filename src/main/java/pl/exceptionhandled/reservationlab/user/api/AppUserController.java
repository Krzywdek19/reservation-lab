package pl.exceptionhandled.reservationlab.user.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.reservationlab.user.dto.AppUserResponse;
import pl.exceptionhandled.reservationlab.user.dto.CreateAppUserRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserFacade appUserFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse createUser(
            @Valid @RequestBody CreateAppUserRequest request
    ) {
        return appUserFacade.createUser(request);
    }

    @GetMapping("/{userId}")
    public AppUserResponse getUser(
            @PathVariable UUID userId
    ) {
        return appUserFacade.getUser(userId);
    }

    @GetMapping
    public List<AppUserResponse> getUsers() {
        return appUserFacade.getUsers();
    }
}