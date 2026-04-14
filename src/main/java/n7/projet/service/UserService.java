package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.User;
import n7.projet.repository.UserRepository;

@Service
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user == null || user.getUsername() == null || user.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email are required");
        }

        String username = user.getUsername().trim();
        String email = user.getEmail().trim();

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username must be alphanumeric only (letters and digits)");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        User existingWithUsername = userRepository.findByUsername(username).orElse(null);
        if (existingWithUsername != null) {
            if (existingWithUsername.getEmail() != null && existingWithUsername.getEmail().equalsIgnoreCase(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "User already exists with the same username and email");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username already exists, please choose another username");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already exists, please choose another email");
        }

        user.setUsername(username);
        user.setEmail(email);

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
