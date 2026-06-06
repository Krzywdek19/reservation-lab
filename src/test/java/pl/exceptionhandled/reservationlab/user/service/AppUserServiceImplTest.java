package pl.exceptionhandled.reservationlab.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.reservationlab.user.AppUser;
import pl.exceptionhandled.reservationlab.user.AppUserRepository;
import pl.exceptionhandled.reservationlab.user.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @Test
    void createUserShouldCreateUser() {
        var command = new CreateAppUserCommand(
                "john@example.com",
                "john"
        );

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = appUserService.createUser(command);

        assertThat(result.getEmail()).isEqualTo(command.email());
        assertThat(result.getUsername()).isEqualTo(command.username());

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void getUserShouldReturnUser() {
        UUID userId = UUID.randomUUID();

        AppUser user = AppUser.builder()
                .email("john@example.com")
                .username("john")
                .build();
        user.setId(userId);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

        AppUser result = appUserService.getUser(userId);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUserShouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUsersShouldReturnAllUsers() {
        AppUser first = AppUser.builder()
                .email("john@example.com")
                .username("john")
                .build();

        AppUser second = AppUser.builder()
                .email("adam@example.com")
                .username("adam")
                .build();

        when(appUserRepository.findAll()).thenReturn(List.of(first, second));

        List<AppUser> result = appUserService.getUsers();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(first, second);
    }
}