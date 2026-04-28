package n7.projet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import n7.projet.entity.Statement;
import n7.projet.service.StatementService;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @PostMapping
    public ResponseEntity<StatementResponse> createStatement(@RequestBody Statement statement) {
        Statement createdStatement = statementService.createStatement(statement);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(createdStatement));
    }

    @GetMapping
    public List<StatementResponse> getAllStatements() {
        return statementService.getAllStatements().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatementResponse> getStatementById(@PathVariable Long id) {
        Statement statement = statementService.getStatementById(id);
        if (statement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(statement));
    }

    @GetMapping("/round/{roundId}")
    public List<StatementResponse> getStatementsByRoundId(@PathVariable Long roundId) {
        return statementService.getStatementsByRoundId(roundId).stream().map(this::toResponse).toList();
    }

    private StatementResponse toResponse(Statement statement) {
        boolean revealLie = statement.getRound() != null && "RESULTS".equals(statement.getRound().getPhase());
        return new StatementResponse(
                statement.getId(),
                statement.getContent(),
                statement.getPosition(),
                revealLie ? statement.isLie() : null);
    }

    public record StatementResponse(Long id, String content, int position, Boolean isLie) {
    }
}