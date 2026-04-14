package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Player;
import n7.projet.entity.Round;
import n7.projet.entity.Statement;
import n7.projet.entity.Vote;
import n7.projet.repository.PlayerRepository;
import n7.projet.repository.RoundRepository;
import n7.projet.repository.StatementRepository;
import n7.projet.repository.VoteRepository;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final RoundRepository roundRepository;
    private final PlayerRepository playerRepository;
    private final StatementRepository statementRepository;

    public VoteService(VoteRepository voteRepository, RoundRepository roundRepository,
            PlayerRepository playerRepository,
            StatementRepository statementRepository) {
        this.voteRepository = voteRepository;
        this.roundRepository = roundRepository;
        this.playerRepository = playerRepository;
        this.statementRepository = statementRepository;
    }

    public Vote createVote(Vote vote) {
        if (vote.getVotedAt() == null) {
            vote.setVotedAt(LocalDateTime.now());
        }
        return voteRepository.save(vote);
    }

    public Vote createVote(Long roundId, Long voterId, Long statementId)
            throws ResponseStatusException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));
        Player voter = playerRepository.findById(voterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found"));
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Statement not found"));

        // Voter cannot be the speaker
        if (round.getSpeakerId() != null && round.getSpeakerId().equals(voterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Speaker cannot vote on their own statements");
        }

        // Check if voter already voted in this round
        if (voteRepository.existsByRoundIdAndVoterId(roundId, voterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Player has already voted in this round");
        }

        // Check round is in VOTING phase
        if (round.getPhase() == null || !round.getPhase().equals("VOTING")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round is not in voting phase");
        }

        Vote vote = new Vote();
        vote.setRound(round);
        vote.setVoter(voter);
        vote.setStatement(statement);
        vote.setVotedAt(LocalDateTime.now());
        return voteRepository.save(vote);
    }

    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    public Vote getVoteById(Long id) {
        return voteRepository.findById(id).orElse(null);
    }

    public long countVotesForStatement(Long roundId, Long statementId) {
        return voteRepository.countByRoundIdAndStatementId(roundId, statementId);
    }

    public boolean hasPlayerVoted(Long roundId, Long voterId) {
        return voteRepository.existsByRoundIdAndVoterId(roundId, voterId);
    }

    public List<Vote> getVotesByRoundId(Long roundId) {
        return voteRepository.findByRoundId(roundId);
    }
}