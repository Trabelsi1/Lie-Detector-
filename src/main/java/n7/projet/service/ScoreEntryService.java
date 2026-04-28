package n7.projet.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import n7.projet.entity.Game;
import n7.projet.entity.Player;
import n7.projet.entity.Round;
import n7.projet.entity.ScoreEntry;
import n7.projet.entity.Statement;
import n7.projet.entity.Vote;
import n7.projet.repository.PlayerRepository;
import n7.projet.repository.ScoreEntryRepository;
import n7.projet.repository.StatementRepository;
import n7.projet.repository.VoteRepository;

@Service
public class ScoreEntryService {

    private final ScoreEntryRepository scoreEntryRepository;
    private final StatementRepository statementRepository;
    private final VoteRepository voteRepository;
    private final PlayerRepository playerRepository;

    public ScoreEntryService(ScoreEntryRepository scoreEntryRepository, StatementRepository statementRepository,
            VoteRepository voteRepository, PlayerRepository playerRepository) {
        this.scoreEntryRepository = scoreEntryRepository;
        this.statementRepository = statementRepository;
        this.voteRepository = voteRepository;
        this.playerRepository = playerRepository;
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

    @Transactional
    public void awardRoundScores(Round round) {
        if (round == null || round.getId() == null || round.getGame() == null || round.getGame().getId() == null) {
            return;
        }

        List<Statement> statements = statementRepository.findByRoundId(round.getId());
        Statement lieStatement = statements.stream().filter(Statement::isLie).findFirst().orElse(null);
        if (lieStatement == null) {
            return;
        }

        List<Vote> votes = voteRepository.findByRoundId(round.getId());
        Set<Long> correctVoterIds = new HashSet<>();
        for (Vote vote : votes) {
            if (vote.getStatement() != null && lieStatement.getId().equals(vote.getStatement().getId())
                    && vote.getVoter() != null) {
                correctVoterIds.add(vote.getVoter().getId());
            }
        }

        Game game = round.getGame();

        for (Long playerId : correctVoterIds) {
            ScoreEntry playerScore = getOrCreateScoreEntry(game, playerId);
            if (playerScore == null) {
                continue;
            }
            playerScore.setCorrectGuesses(playerScore.getCorrectGuesses() + 1);
            playerScore.setPoints(playerScore.getPoints() + 1);
            scoreEntryRepository.save(playerScore);
        }

        long fooledPlayers = votes.size() - correctVoterIds.size();
        if (round.getSpeakerId() != null) {
            ScoreEntry speakerScore = getOrCreateScoreEntry(game, round.getSpeakerId());
            if (speakerScore == null) {
                return;
            }
            speakerScore.setPlayersFooled(speakerScore.getPlayersFooled() + (int) fooledPlayers);
            speakerScore.setPoints(speakerScore.getPoints() + (int) fooledPlayers);
            scoreEntryRepository.save(speakerScore);
        }
    }

    private ScoreEntry getOrCreateScoreEntry(Game game, Long playerId) {
        return scoreEntryRepository.findByGameIdAndPlayerId(game.getId(), playerId)
                .orElseGet(() -> {
                    ScoreEntry scoreEntry = new ScoreEntry();
                    scoreEntry.setGame(game);
                    Player player = playerRepository.findById(playerId).orElse(null);
                    if (player == null) {
                        return null;
                    }
                    scoreEntry.setPlayer(player);
                    scoreEntry.setPoints(0);
                    scoreEntry.setCorrectGuesses(0);
                    scoreEntry.setPlayersFooled(0);
                    return scoreEntry;
                });
    }
}