package n7.projet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ScoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int points;
    private int correctGuesses;
    private int playersFooled;

    @ManyToOne
    private Game game;

    @ManyToOne
    private User user;

    public ScoreEntry() {
    }

    public ScoreEntry(int points, int correctGuesses, int playersFooled, Game game, User user) {
        this.points = points;
        this.correctGuesses = correctGuesses;
        this.playersFooled = playersFooled;
        this.game = game;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCorrectGuesses() {
        return correctGuesses;
    }

    public void setCorrectGuesses(int correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    public int getPlayersFooled() {
        return playersFooled;
    }

    public void setPlayersFooled(int playersFooled) {
        this.playersFooled = playersFooled;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}