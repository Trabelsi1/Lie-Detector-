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
    public ResponseEntity<Statement> createStatement(@RequestBody Statement statement) {
        Statement createdStatement = statementService.createStatement(statement);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStatement);
    }

    @GetMapping
    public List<Statement> getAllStatements() {
        return statementService.getAllStatements();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Statement> getStatementById(@PathVariable Long id) {
        Statement statement = statementService.getStatementById(id);
        if (statement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/round/{roundId}")
    public List<Statement> getStatementsByRoundId(@PathVariable Long roundId) {
        return statementService.getStatementsByRoundId(roundId);
    }
}