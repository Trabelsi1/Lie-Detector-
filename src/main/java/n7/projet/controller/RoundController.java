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

import n7.projet.entity.Round;
import n7.projet.service.RoundService;

@RestController
@RequestMapping("/api/rounds")
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @PostMapping
    public ResponseEntity<Round> createRound(@RequestBody Round round) {
        Round createdRound = roundService.createRound(round);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRound);
    }

    @GetMapping
    public List<Round> getAllRounds() {
        return roundService.getAllRounds();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Round> getRoundById(@PathVariable Long id) {
        Round round = roundService.getRoundById(id);
        if (round == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(round);
    }

    @GetMapping("/game/{gameId}")
    public List<Round> getRoundsByGameId(@PathVariable Long gameId) {
        return roundService.getRoundsByGameId(gameId);
    }

    @GetMapping("/game/{gameId}/number/{roundNumber}")
    public ResponseEntity<Round> getRoundByGameIdAndRoundNumber(@PathVariable Long gameId,
            @PathVariable int roundNumber) {
        Round round = roundService.getRoundByGameIdAndRoundNumber(gameId, roundNumber);
        if (round == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(round);
    }
}