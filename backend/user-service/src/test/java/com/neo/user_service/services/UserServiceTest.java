package com.neo.user_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.ChangePasswordRequest;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.exceptions.auth.IncorrectPasswordException;
import com.neo.user_service.exceptions.user.AdminDeletionNotAllowedException;
import com.neo.user_service.exceptions.user.UserNotFoundException;
import com.neo.user_service.models.User;
import com.neo.user_service.models.UserRole;
import com.neo.user_service.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .firstName("Hamza")
                .lastName("Elkhawlani")
                .username("heeemzaaa")
                .email("hamza@example.com")
                .password("hashed-password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void getMe_returnsUser_whenUserExists() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        ResponseData<UserResponse> response = userService.getMe("user-1");

        assertEquals("heeemzaaa", response.getData().username());
    }

    @Test
    void getMe_throwsException_whenUserNotFound() {
        when(userRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getMe("missing-id"));
    }

    @Test
    void deleteMe_deletesUser_whenNotAdmin() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        userService.deleteMe("user-1");

        verify(userRepository).deleteById("user-1");
    }

    @Test
    void deleteMe_throwsException_whenUserIsAdmin() {
        user.setRole(UserRole.ADMIN);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThrows(AdminDeletionNotAllowedException.class, () -> userService.deleteMe("user-1"));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void changePassword_updatesPassword_whenCurrentPasswordCorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "hashed-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hashed-password");

        userService.changePassword("user-1", request);

        verify(userRepository).save(user);
        assertEquals("new-hashed-password", user.getPassword());
    }

    @Test
    void changePassword_throwsException_whenCurrentPasswordWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-password");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(IncorrectPasswordException.class, () -> userService.changePassword("user-1", request));
        verify(userRepository, never()).save(any());
    }
}
