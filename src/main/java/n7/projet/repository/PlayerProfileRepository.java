package n7.projet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.PlayerProfile;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByPlayerId(Long playerId);
}