package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Round;
import n7.projet.entity.Game;
import n7.projet.entity.Statement;
import n7.projet.repository.GameRepository;
import n7.projet.repository.RoundRepository;
import n7.projet.repository.VoteRepository;
import n7.projet.repository.StatementRepository;

@Service
public class RoundService {

    private final RoundRepository roundRepository;
    private final VoteRepository voteRepository;
    private final ScoreEntryService scoreEntryService;
    private final GameRepository gameRepository;
    private final GameAwardsService gameAwardsService;
    private final StatementRepository statementRepository;

    public RoundService(RoundRepository roundRepository, VoteRepository voteRepository,
            ScoreEntryService scoreEntryService, GameRepository gameRepository,
            GameAwardsService gameAwardsService, StatementRepository statementRepository) {
        this.roundRepository = roundRepository;
        this.voteRepository = voteRepository;
        this.scoreEntryService = scoreEntryService;
        this.gameRepository = gameRepository;
        this.gameAwardsService = gameAwardsService;
        this.statementRepository = statementRepository;
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
            // Validate exactly 3 statements with exactly 1 lie before advancing to
            // discussion
            // Query database directly to get fresh data (not cached Set)
            List<Statement> statements = statementRepository.findByRoundIdOrderByPositionAsc(roundId);
            if (statements == null || statements.size() != 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Round must have exactly 3 statements before advancing");
            }
            long liesCount = statements.stream().filter(Statement::isLie).count();
            if (liesCount != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Round must have exactly one lie before advancing to discussion");
            }
            nextPhase = "DISCUSSION";
        } else if ("DISCUSSION".equals(currentPhase)) {
            nextPhase = "VOTING";
        } else if ("VOTING".equals(currentPhase)) {
            VotingStatus status = getVotingStatus(roundId);
            if (!status.complete()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot advance to results until all eligible players have voted");
            }
            nextPhase = "RESULTS";
            scoreEntryService.awardRoundScores(round);
            round.setEndedAt(LocalDateTime.now());

            Game game = round.getGame();
            if (game != null) {
                int targetCycles = game.getTargetCycles() > 0 ? Math.min(game.getTargetCycles(), 10) : 2;
                if (game.getCurrentCycle() <= 0) {
                    game.setCurrentCycle(1);
                }
                if (game.getCycleStartPlayerCount() <= 0 && game.getGameRoom() != null
                        && game.getGameRoom().getPlayers() != null) {
                    game.setCycleStartPlayerCount(game.getGameRoom().getPlayers().size());
                }

                int cyclePlayerCount = Math.max(0, game.getCycleStartPlayerCount());
                int completedInCycle = Math.max(0, game.getSpeakersCompletedInCycle());
                if (cyclePlayerCount > 0 && completedInCycle < cyclePlayerCount) {
                    completedInCycle++;
                }

                game.setSpeakersCompletedInCycle(completedInCycle);
                game.setCurrentRoundIndex(Math.max(0, game.getCurrentRoundIndex()) + 1);

                boolean cycleFinished = cyclePlayerCount > 0 && completedInCycle >= cyclePlayerCount;
                if (cycleFinished) {
                    if (game.getCurrentCycle() >= targetCycles) {
                        game.setStatus("COMPLETED");
                        if (game.getEndTime() == null) {
                            game.setEndTime(LocalDateTime.now());
                        }
                    } else {
                        game.setCurrentCycle(game.getCurrentCycle() + 1);
                        game.setSpeakersCompletedInCycle(0);
                        game.setCycleStartPlayerCount(0);
                    }
                }

                game.setTargetCycles(targetCycles);
                gameRepository.save(game);

                if ("COMPLETED".equalsIgnoreCase(String.valueOf(game.getStatus()))) {
                    gameAwardsService.applyLifetimeStatsIfCompleted(game.getId());
                }
            }
        } else if ("RESULTS".equals(currentPhase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round already completed. Start a new round.");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown phase: " + currentPhase);
        }

        round.setPhase(nextPhase);
        return roundRepository.save(round);
    }

    public VotingStatus getVotingStatus(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        int requiredVotes = 0;
        if (round.getGame() != null && round.getGame().getGameRoom() != null
                && round.getGame().getGameRoom().getPlayers() != null) {
            requiredVotes = (int) round.getGame().getGameRoom().getPlayers().stream()
                    .filter(player -> player != null && player.getId() != null
                            && !player.getId().equals(round.getSpeakerId()))
                    .count();
        }

        long submittedVotes = voteRepository.countByRoundId(roundId);
        boolean complete = submittedVotes >= requiredVotes;

        return new VotingStatus(requiredVotes, submittedVotes, complete);
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

    public record VotingStatus(int requiredVotes, long submittedVotes, boolean complete) {
    }
}