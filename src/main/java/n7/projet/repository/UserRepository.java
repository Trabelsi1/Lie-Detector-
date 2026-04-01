package n7.projet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);
}
