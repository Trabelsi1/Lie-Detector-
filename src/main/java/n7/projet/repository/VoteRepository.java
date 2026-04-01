package n7.projet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    long countByRoundIdAndStatementId(Long roundId, Long statementId);

    boolean existsByRoundIdAndVoterId(Long roundId, Long voterId);
}
