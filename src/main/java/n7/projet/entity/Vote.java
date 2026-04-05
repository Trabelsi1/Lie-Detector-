package n7.projet.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime votedAt;

    @ManyToOne
    private Player voter;

    @ManyToOne
    private Round round;

    @ManyToOne
    private Statement statement;

    public Vote() {
    }

    public Vote(LocalDateTime votedAt, Player voter, Round round, Statement statement) {
        this.votedAt = votedAt;
        this.voter = voter;
        this.round = round;
        this.statement = statement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }

    public Player getVoter() {
        return voter;
    }

    public void setVoter(Player voter) {
        this.voter = voter;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
