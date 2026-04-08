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

import n7.projet.entity.ScoreEntry;
import n7.projet.service.ScoreEntryService;

@RestController
@RequestMapping("/api/scores")
public class ScoreEntryController {

    private final ScoreEntryService scoreEntryService;

    public ScoreEntryController(ScoreEntryService scoreEntryService) {
        this.scoreEntryService = scoreEntryService;
    }

    @PostMapping
    public ResponseEntity<ScoreEntry> createScoreEntry(@RequestBody ScoreEntry scoreEntry) {
        ScoreEntry createdScoreEntry = scoreEntryService.createScoreEntry(scoreEntry);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdScoreEntry);
    }

    @GetMapping
    public List<ScoreEntry> getAllScoreEntries() {
        return scoreEntryService.getAllScoreEntries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScoreEntry> getScoreEntryById(@PathVariable Long id) {
        ScoreEntry scoreEntry = scoreEntryService.getScoreEntryById(id);
        if (scoreEntry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scoreEntry);
    }

    @GetMapping("/game/{gameId}")
    public List<ScoreEntry> getScoreEntriesByGameId(@PathVariable Long gameId) {
        return scoreEntryService.getScoreEntriesByGameId(gameId);
    }

    @GetMapping("/player/{playerId}")
    public List<ScoreEntry> getScoreEntriesByPlayerId(@PathVariable Long playerId) {
        return scoreEntryService.getScoreEntriesByPlayerId(playerId);
    }
}