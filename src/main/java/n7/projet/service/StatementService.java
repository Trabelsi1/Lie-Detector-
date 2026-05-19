package n7.projet.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Statement;
import n7.projet.repository.StatementRepository;

@Service
public class StatementService {

    private final StatementRepository statementRepository;
    private final RoundService roundService;

    public StatementService(StatementRepository statementRepository, RoundService roundService) {
        this.statementRepository = statementRepository;
        this.roundService = roundService;
    }

    public Statement createStatement(Statement statement) {
        if (statement.getRound() == null || statement.getRound().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Round is required");
        }

        if (!roundService.isInStatementSubmissionPhase(statement.getRound().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round is not in statement submission phase");
        }

        List<Statement> existingStatements = statementRepository
                .findByRoundIdOrderByPositionAsc(statement.getRound().getId());
        if (existingStatements.size() >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum 3 statements per round reached");
        }

        if (statement.isLie()) {
            long liesCount = existingStatements.stream().filter(Statement::isLie).count();
            if (liesCount > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "There can only be one lie per round");
            }
        }

        if (statement.getPosition() == 0) {
            statement.setPosition(existingStatements.size() + 1);
        }

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

    public void deleteStatement(Long statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Statement not found"));

        if (statement.getRound() != null && !roundService.isInStatementSubmissionPhase(statement.getRound().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete statements after statement submission phase");
        }

        Long roundId = statement.getRound().getId();
        statementRepository.deleteById(statementId);

        List<Statement> remainingStatements = statementRepository.findByRoundIdOrderByPositionAsc(roundId);
        for (int i = 0; i < remainingStatements.size(); i++) {
            remainingStatements.get(i).setPosition(i + 1);
            statementRepository.save(remainingStatements.get(i));
        }
    }
}