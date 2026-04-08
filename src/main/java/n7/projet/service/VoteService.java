package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

    public Vote createVote(Long roundId, Long voterId, Long statementId) {
        Round round = roundRepository.findById(roundId).orElse(null);
        Player voter = playerRepository.findById(voterId).orElse(null);
        Statement statement = statementRepository.findById(statementId).orElse(null);

        if (round == null || voter == null || statement == null) {
            return null;
        }
        if (voteRepository.existsByRoundIdAndVoterId(roundId, voterId)) {
            return null;
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
}