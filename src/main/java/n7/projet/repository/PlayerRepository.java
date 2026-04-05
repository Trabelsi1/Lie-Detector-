package n7.projet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUserId(Long userId);
}
