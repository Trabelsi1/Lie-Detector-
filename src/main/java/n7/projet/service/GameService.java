package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Game;
import n7.projet.entity.GameRoom;
import n7.projet.entity.Player;
import n7.projet.entity.Round;
import n7.projet.repository.GameRepository;
import n7.projet.repository.GameRoomRepository;
import n7.projet.repository.PlayerRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository;
    private final RoundService roundService;

    public GameService(GameRepository gameRepository, GameRoomRepository gameRoomRepository,
            PlayerRepository playerRepository, RoundService roundService) {
        this.gameRepository = gameRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.playerRepository = playerRepository;
        this.roundService = roundService;
    }

    public Game createGame(Game game) {
        if (game.getStartTime() == null) {
            game.setStartTime(LocalDateTime.now());
        }
        if (game.getStatus() == null) {
            game.setStatus("ONGOING");
        }
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

        List<Player> players = room.getPlayers().stream().toList();
        int nextSpeakerIndex = game.getCurrentRoundIndex() % players.size();
        Player speaker = players.get(nextSpeakerIndex);

        Round round = new Round();
        round.setRoundNumber(game.getCurrentRoundIndex() + 1);
        round.setPhase("STATEMENT_SUBMISSION");
        round.setSpeakerId(speaker.getId());
        round.setSpeaker(speaker);
        round.setGame(game);
        round.setStartedAt(LocalDateTime.now());

        Round savedRound = roundService.createRound(round);

        // Increment round counter
        game.setCurrentRoundIndex(game.getCurrentRoundIndex() + 1);
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
}