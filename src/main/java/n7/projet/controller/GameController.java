package n7.projet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import n7.projet.entity.Game;
import n7.projet.entity.Round;
import n7.projet.service.GameAwardsService;
import n7.projet.service.GameService;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameAwardsService gameAwardsService;

    public GameController(GameService gameService, GameAwardsService gameAwardsService) {
        this.gameService = gameService;
        this.gameAwardsService = gameAwardsService;
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Game game) {
        Game createdGame = gameService.createGame(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGame);
    }

    @GetMapping
    public List<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Game game = gameService.getGameById(id);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/room/{roomId}")
    public List<Game> getGamesByRoomId(@PathVariable Long roomId) {
        return gameService.getGamesByRoomId(roomId);
    }

    @PostMapping("/{gameId}/start-round")
    public ResponseEntity<Round> startRound(@PathVariable Long gameId) {
        Round round = gameService.startRound(gameId);
        return ResponseEntity.status(HttpStatus.CREATED).body(round);
    }

    @GetMapping("/{gameId}/current-round")
    public ResponseEntity<Round> getCurrentRound(@PathVariable Long gameId) {
        Round round = gameService.getCurrentRound(gameId);
        if (round == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(round);
    }

    @GetMapping("/{gameId}/all-speakers-done")
    public ResponseEntity<Boolean> allSpeakersDone(@PathVariable Long gameId) {
        boolean done = gameService.allPlayersSpeakingDone(gameId);
        return ResponseEntity.ok(done);
    }

    @GetMapping("/{gameId}/speaker-progress")
    public ResponseEntity<GameService.SpeakerProgress> getSpeakerProgress(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getSpeakerProgress(gameId));
    }

    @GetMapping("/{gameId}/final-rankings")
    public ResponseEntity<List<GameService.PlayerScore>> getFinalRankings(@PathVariable Long gameId) {
        List<GameService.PlayerScore> rankings = gameService.getFinalRankings(gameId);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/{gameId}/final-summary")
    public ResponseEntity<GameAwardsService.FinalSummary> getFinalSummary(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameAwardsService.getFinalSummary(gameId));
    }
}