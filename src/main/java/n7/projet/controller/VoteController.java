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

import n7.projet.entity.Vote;
import n7.projet.service.VoteService;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<Vote> createVote(@RequestBody Vote vote) {
        Vote createdVote = voteService.createVote(vote);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVote);
    }

    @PostMapping("/round/{roundId}/voter/{voterId}/statement/{statementId}")
    public ResponseEntity<Vote> createVote(@PathVariable Long roundId, @PathVariable Long voterId,
            @PathVariable Long statementId) {
        Vote createdVote = voteService.createVote(roundId, voterId, statementId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVote);
    }

    @GetMapping
    public List<Vote> getAllVotes() {
        return voteService.getAllVotes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vote> getVoteById(@PathVariable Long id) {
        Vote vote = voteService.getVoteById(id);
        if (vote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vote);
    }

    @GetMapping("/round/{roundId}")
    public List<Vote> getVotesByRoundId(@PathVariable Long roundId) {
        return voteService.getVotesByRoundId(roundId);
    }

    @GetMapping("/round/{roundId}/statement/{statementId}/count")
    public long countVotesForStatement(@PathVariable Long roundId, @PathVariable Long statementId) {
        return voteService.countVotesForStatement(roundId, statementId);
    }

    @GetMapping("/round/{roundId}/voter/{voterId}")
    public boolean hasPlayerVoted(@PathVariable Long roundId, @PathVariable Long voterId) {
        return voteService.hasPlayerVoted(roundId, voterId);
    }
}