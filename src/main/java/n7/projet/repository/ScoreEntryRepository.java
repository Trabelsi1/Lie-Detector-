package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.ScoreEntry;

public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    List<ScoreEntry> findByGameId(Long gameId);

    List<ScoreEntry> findByPlayerId(Long playerId);
}