package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        if (round.getPhase() == null) {
            round.setPhase("STATEMENT_SUBMISSION");
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

    /**
     * Advances the round phase to the next state. Valid transitions:
     * STATEMENT_SUBMISSION -> DISCUSSION -> VOTING -> RESULTS
     */
    public Round advancePhase(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        String currentPhase = round.getPhase();
        String nextPhase;

        if ("STATEMENT_SUBMISSION".equals(currentPhase)) {
            nextPhase = "DISCUSSION";
        } else if ("DISCUSSION".equals(currentPhase)) {
            nextPhase = "VOTING";
        } else if ("VOTING".equals(currentPhase)) {
            nextPhase = "RESULTS";
        } else if ("RESULTS".equals(currentPhase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round already completed. Start a new round.");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown phase: " + currentPhase);
        }

        round.setPhase(nextPhase);
        return roundRepository.save(round);
    }

    /**
     * Checks if a round is still in STATEMENT_SUBMISSION phase (statements can be
     * added).
     */
    public boolean isInStatementSubmissionPhase(Long roundId) {
        Round round = roundRepository.findById(roundId).orElse(null);
        return round != null && "STATEMENT_SUBMISSION".equals(round.getPhase());
    }

    /**
     * Checks if a round is in VOTING phase.
     */
    public boolean isInVotingPhase(Long roundId) {
        Round round = roundRepository.findById(roundId).orElse(null);
        return round != null && "VOTING".equals(round.getPhase());
    }
}