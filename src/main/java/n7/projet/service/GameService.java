package n7.projet.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Game;
import n7.projet.entity.GameRoom;
import n7.projet.entity.Player;
import n7.projet.entity.Round;
import n7.projet.entity.ScoreEntry;
import n7.projet.repository.GameRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RoundService roundService;

    public GameService(GameRepository gameRepository, RoundService roundService) {
        this.gameRepository = gameRepository;
        this.roundService = roundService;
    }

    public Game createGame(Game game) {
        if (game.getStartTime() == null) {
            game.setStartTime(LocalDateTime.now());
        }
        if (game.getStatus() == null) {
            game.setStatus("ONGOING");
        }
        if (game.getTargetCycles() <= 0) {
            game.setTargetCycles(2);
        }
        if (game.getTargetCycles() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target cycles cannot exceed 10");
        }
        if (game.getCurrentCycle() <= 0) {
            game.setCurrentCycle(1);
        }
        game.setCurrentRoundIndex(0);
        game.setCycleStartPlayerCount(0);
        game.setSpeakersCompletedInCycle(0);
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    public List<Game> getGamesByRoomId(Long roomId) {
        return gameRepository.findByGameRoomId(roomId);
    }

    /**
     * Starts a new round in the game by assigning the next speaker (round-robin).
     */
    public Round startRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        GameRoom room = game.getGameRoom();
        if (room == null || room.getPlayers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No players in game room");
        }

        Round latestRound = getCurrentRound(gameId);
        if (latestRound != null && !"RESULTS".equals(latestRound.getPhase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot start a new round while current round is still active");
        }

        SpeakerProgress progress = getSpeakerProgress(gameId);
        if (progress.allSpeakersDone()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "All configured cycles are complete for this game");
        }

        List<Player> players = room.getPlayers();
        if (game.getTargetCycles() <= 0) {
            game.setTargetCycles(2);
        }
        if (game.getCurrentCycle() <= 0) {
            game.setCurrentCycle(1);
        }

        if (game.getCycleStartPlayerCount() <= 0) {
            game.setCycleStartPlayerCount(players.size());
        }

        int eligibleSpeakersThisCycle = Math.min(game.getCycleStartPlayerCount(), players.size());
        if (eligibleSpeakersThisCycle <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No eligible speakers in current cycle");
        }

        int nextSpeakerIndex = game.getSpeakersCompletedInCycle();
        if (nextSpeakerIndex >= eligibleSpeakersThisCycle) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Current cycle is already complete; finish the transition first");
        }

        Player speaker = players.get(nextSpeakerIndex);

        Round round = new Round();
        round.setRoundNumber(game.getRounds() == null ? 1 : game.getRounds().size() + 1);
        round.setPhase("STATEMENT_SUBMISSION");
        round.setSpeakerId(speaker.getId());
        round.setSpeaker(speaker);
        round.setGame(game);
        round.setStartedAt(LocalDateTime.now());

        Round savedRound = roundService.createRound(round);
        // Keep this legacy value as completed speaker-turns across all cycles.
        game.setCurrentRoundIndex(progress.totalCompletedTurns());
        gameRepository.save(game);

        return savedRound;
    }

    /**
     * Gets the current round of the game (latest round by round number).
     */
    public Round getCurrentRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        return game.getRounds().stream()
                .max((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                .orElse(null);
    }

    /**
     * Checks if all players have been speakers at least once.
     * Each player takes one turn as a speaker in round-robin fashion.
     */
    public boolean allPlayersSpeakingDone(Long gameId) {
        return getSpeakerProgress(gameId).allSpeakersDone();
    }

    public SpeakerProgress getSpeakerProgress(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        int playersInRoomNow = (game.getGameRoom() != null && game.getGameRoom().getPlayers() != null)
                ? game.getGameRoom().getPlayers().size()
                : 0;

        int targetCycles = game.getTargetCycles() > 0 ? game.getTargetCycles() : 2;
        if (targetCycles > 10) {
            targetCycles = 10;
        }

        if (game.getCurrentCycle() <= 0) {
            game.setCurrentCycle(1);
        }

        int cyclePlayerCount = game.getCycleStartPlayerCount() > 0
                ? game.getCycleStartPlayerCount()
                : playersInRoomNow;

        if (cyclePlayerCount < 0) {
            cyclePlayerCount = 0;
        }

        int completedInCycle = Math.max(0, game.getSpeakersCompletedInCycle());
        if (cyclePlayerCount > 0 && completedInCycle > cyclePlayerCount) {
            completedInCycle = cyclePlayerCount;
        }

        boolean allCyclesDone = "COMPLETED".equalsIgnoreCase(String.valueOf(game.getStatus()))
                || (game.getCurrentCycle() >= targetCycles && cyclePlayerCount > 0 && completedInCycle >= cyclePlayerCount);

        if (allCyclesDone) {
            game.setStatus("COMPLETED");
            if (game.getEndTime() == null) {
                game.setEndTime(LocalDateTime.now());
            }
        }

        int completedCycles = Math.max(0, game.getCurrentCycle() - 1);
        int totalCompletedTurns = game.getCurrentRoundIndex();
        if (totalCompletedTurns < 0) {
            totalCompletedTurns = 0;
        }

        game.setTargetCycles(targetCycles);
        game.setCycleStartPlayerCount(cyclePlayerCount);
        game.setSpeakersCompletedInCycle(completedInCycle);
        gameRepository.save(game);

        return new SpeakerProgress(
                completedInCycle,
                cyclePlayerCount,
                allCyclesDone,
                game.getCurrentCycle(),
                targetCycles,
                completedCycles,
                totalCompletedTurns,
                playersInRoomNow);
    }

    /**
     * Gets the final rankings by total score for all players in the game.
     */
    public List<PlayerScore> getFinalRankings(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Player> players = game.getGameRoom().getPlayers();
        List<PlayerScore> rankings = new ArrayList<>();

        for (Player player : players) {
            long totalScore = game.getScoreEntries().stream()
                    .filter(entry -> entry.getPlayer().getId().equals(player.getId()))
                    .mapToLong(ScoreEntry::getPoints)
                    .sum();
            rankings.add(new PlayerScore(player.getId(), player.getUser().getUsername(), totalScore));
        }

        // Sort by score descending
        rankings.sort((a, b) -> Long.compare(b.score(), a.score()));
        return rankings;
    }

    public record PlayerScore(Long playerId, String playerName, long score) {
    }

    public record SpeakerProgress(
            int completedSpeakers,
            int totalPlayers,
            boolean allSpeakersDone,
            int currentCycle,
            int targetCycles,
            int completedCycles,
            int totalCompletedTurns,
            int playersInRoomNow) {
    }
}