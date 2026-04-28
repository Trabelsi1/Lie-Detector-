package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    long countByRoundIdAndStatementId(Long roundId, Long statementId);

    long countByRoundId(Long roundId);

    boolean existsByRoundIdAndVoterId(Long roundId, Long voterId);

    List<Vote> findByRoundId(Long roundId);
}
