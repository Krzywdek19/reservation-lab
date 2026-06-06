package pl.exceptionhandled.reservationlab.user.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.user.dto.AppUserResponse;
import pl.exceptionhandled.reservationlab.user.dto.CreateAppUserRequest;
import pl.exceptionhandled.reservationlab.user.mapper.AppUserMapper;
import pl.exceptionhandled.reservationlab.user.service.AppUserService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppUserFacade {
    private final AppUserMapper mapper;
    private final AppUserService service;

    public AppUserResponse createUser(CreateAppUserRequest request) {
        var command = mapper.toCommand(request);
        return mapper.toResponse(service.createUser(command));
    }

    public AppUserResponse getUser(UUID id) {
        return mapper.toResponse(service.getUser(id));
    }

    public List<AppUserResponse> getUsers() {
        return mapper.toResponseList(service.getUsers());
    }
}
