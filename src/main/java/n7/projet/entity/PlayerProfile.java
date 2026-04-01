package n7.projet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double deceptionRate;

    private double detectionRate;

    private String archetype;

    private int totalGames;
    private int totalWins;
    private int totalPlayersFooled;

    @OneToOne
    private User user;

    public PlayerProfile() {
    }

    public PlayerProfile(double deceptionRate, double detectionRate, String archetype, int totalGames, int totalWins,
            int totalPlayersFooled) {
        this.deceptionRate = deceptionRate;
        this.detectionRate = detectionRate;
        this.archetype = archetype;
        this.totalGames = totalGames;
        this.totalWins = totalWins;
        this.totalPlayersFooled = totalPlayersFooled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public double getDeceptionRate() {
        return deceptionRate;
    }

    public void setDeceptionRate(double deceptionRate) {
        this.deceptionRate = deceptionRate;
    }

    public double getDetectionRate() {
        return detectionRate;
    }

    public void setDetectionRate(double detectionRate) {
        this.detectionRate = detectionRate;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalPlayersFooled() {
        return totalPlayersFooled;
    }

    public void setTotalPlayersFooled(int totalPlayersFooled) {
        this.totalPlayersFooled = totalPlayersFooled;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}