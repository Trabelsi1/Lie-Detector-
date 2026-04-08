package n7.projet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.Statement;
import n7.projet.repository.StatementRepository;

@Service
public class StatementService {

    private final StatementRepository statementRepository;

    public StatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public Statement createStatement(Statement statement) {
        return statementRepository.save(statement);
    }

    public List<Statement> getAllStatements() {
        return statementRepository.findAll();
    }

    public Statement getStatementById(Long id) {
        return statementRepository.findById(id).orElse(null);
    }

    public List<Statement> getStatementsByRoundId(Long roundId) {
        return statementRepository.findByRoundIdOrderByPositionAsc(roundId);
    }
}