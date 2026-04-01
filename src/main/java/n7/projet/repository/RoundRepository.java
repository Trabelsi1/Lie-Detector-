package n7.projet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Round;

public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByGameIdOrderByRoundNumberAsc(Long gameId);

    Optional<Round> findByGameIdAndRoundNumber(Long gameId, int roundNumber);
}
