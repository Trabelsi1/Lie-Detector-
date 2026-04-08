package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.Round;
import n7.projet.repository.RoundRepository;

@Service
public class RoundService {

    private final RoundRepository roundRepository;

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public Round createRound(Round round) {
        if (round.getStartedAt() == null) {
            round.setStartedAt(LocalDateTime.now());
        }
        return roundRepository.save(round);
    }

    public List<Round> getAllRounds() {
        return roundRepository.findAll();
    }

    public Round getRoundById(Long id) {
        return roundRepository.findById(id).orElse(null);
    }

    public List<Round> getRoundsByGameId(Long gameId) {
        return roundRepository.findByGameIdOrderByRoundNumberAsc(gameId);
    }

    public Round getRoundByGameIdAndRoundNumber(Long gameId, int roundNumber) {
        return roundRepository.findByGameIdAndRoundNumber(gameId, roundNumber).orElse(null);
    }
}