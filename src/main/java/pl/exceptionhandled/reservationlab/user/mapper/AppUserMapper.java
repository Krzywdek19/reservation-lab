package pl.exceptionhandled.reservationlab.user.mapper;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.dto.AppUserResponse;
import pl.exceptionhandled.reservationlab.user.dto.CreateAppUserRequest;
import pl.exceptionhandled.reservationlab.user.service.CreateAppUserCommand;

import java.util.List;

@Component
public class AppUserMapper {
    public CreateAppUserCommand toCommand(CreateAppUserRequest request) {
        return new CreateAppUserCommand(
                request.email(),
                request.username()
        );
    }

    public AppUserResponse toResponse(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }

    public List<AppUserResponse> toResponseList(List<AppUser> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }
}
