package n7.projet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.User;
import n7.projet.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
    }

    @Test
    void createUserShouldSetCreatedAtWhenMissing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser.getCreatedAt());
        assertEquals("alice", createdUser.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void getUserByIdShouldReturnUserWhenFound() {
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }

    @Test
    void createUserShouldRejectNonAlphanumericUsername() {
        user.setUsername("alice_01");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(user));
    }

    @Test
    void createUserShouldRejectInvalidEmailFormat() {
        user.setEmail("alice@example");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(user));
    }

    @Test
    void createUserShouldRejectSameUsernameAndEmail() {
        User existingUser = new User();
        existingUser.setUsername("alice");
        existingUser.setEmail("alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(user));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("User already exists with the same username and email", exception.getReason());
    }

    @Test
    void createUserShouldRejectExistingUsernameWithDifferentEmail() {
        User existingUser = new User();
        existingUser.setUsername("alice");
        existingUser.setEmail("other@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(user));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username already exists, please choose another username", exception.getReason());
    }
}