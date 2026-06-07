package pl.exceptionhandled.reservationlab.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    public AppUser createUser(@Valid CreateAppUserCommand command) {
        AppUser user = AppUser.builder()
                .email(command.email())
                .username(command.username())
                .build();

        return appUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> getUsers() {
        return appUserRepository.findAll();
    }
}