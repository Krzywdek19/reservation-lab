package pl.exceptionhandled.reservationlab.user.service;

import pl.exceptionhandled.reservationlab.user.AppUser;

import java.util.List;
import java.util.UUID;

public interface AppUserService {

    AppUser createUser(CreateAppUserCommand command);

    AppUser getUser(UUID userId);

    List<AppUser> getUsers();
}