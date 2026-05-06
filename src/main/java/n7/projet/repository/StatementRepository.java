package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Statement;

public interface StatementRepository extends JpaRepository<Statement, Long> {
    List<Statement> findByRoundId(Long roundId);

    List<Statement> findByRoundIdOrderByPositionAsc(Long roundId);
}
