package n7.projet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.ScoreEntry;
import n7.projet.repository.ScoreEntryRepository;

@Service
public class ScoreEntryService {

    private final ScoreEntryRepository scoreEntryRepository;

    public ScoreEntryService(ScoreEntryRepository scoreEntryRepository) {
        this.scoreEntryRepository = scoreEntryRepository;
    }

    public ScoreEntry createScoreEntry(ScoreEntry scoreEntry) {
        return scoreEntryRepository.save(scoreEntry);
    }

    public List<ScoreEntry> getAllScoreEntries() {
        return scoreEntryRepository.findAll();
    }

    public ScoreEntry getScoreEntryById(Long id) {
        return scoreEntryRepository.findById(id).orElse(null);
    }

    public List<ScoreEntry> getScoreEntriesByGameId(Long gameId) {
        return scoreEntryRepository.findByGameId(gameId);
    }

    public List<ScoreEntry> getScoreEntriesByPlayerId(Long playerId) {
        return scoreEntryRepository.findByPlayerId(playerId);
    }
}