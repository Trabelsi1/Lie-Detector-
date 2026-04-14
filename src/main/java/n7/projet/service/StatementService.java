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

        // Validate round is in STATEMENT_SUBMISSION phase
        if (!roundService.isInStatementSubmissionPhase(statement.getRound().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round is not in statement submission phase");
        }

        // Check max 3 statements per round
        List<Statement> existingStatements = statementRepository
                .findByRoundIdOrderByPositionAsc(statement.getRound().getId());
        if (existingStatements.size() >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum 3 statements per round reached");
        }

        // Auto-assign position if not set
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
}