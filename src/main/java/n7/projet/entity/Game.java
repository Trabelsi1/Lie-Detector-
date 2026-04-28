package n7.projet.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int currentRoundIndex;

    @Column(nullable = false, columnDefinition = "integer default 2")
    private int targetCycles;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private int currentCycle;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int cycleStartPlayerCount;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int speakersCompletedInCycle;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean profileStatsApplied;

    @ManyToOne
    private GameRoom gameRoom;

    @OneToMany(mappedBy = "game")
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "game")
    private Set<ScoreEntry> scoreEntries = new HashSet<>();

    public Game() {
    }

    public Game(LocalDateTime startTime, LocalDateTime endTime, String status, int currentRoundIndex,
            int targetCycles, int currentCycle, int cycleStartPlayerCount, int speakersCompletedInCycle,
            GameRoom gameRoom) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.currentRoundIndex = currentRoundIndex;
        this.targetCycles = targetCycles;
        this.currentCycle = currentCycle;
        this.cycleStartPlayerCount = cycleStartPlayerCount;
        this.speakersCompletedInCycle = speakersCompletedInCycle;
        this.profileStatsApplied = false;
        this.gameRoom = gameRoom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrentRoundIndex() {
        return currentRoundIndex;
    }

    public void setCurrentRoundIndex(int currentRoundIndex) {
        this.currentRoundIndex = currentRoundIndex;
    }

    public int getTargetCycles() {
        return targetCycles;
    }

    public void setTargetCycles(int targetCycles) {
        this.targetCycles = targetCycles;
    }

    public int getCurrentCycle() {
        return currentCycle;
    }

    public void setCurrentCycle(int currentCycle) {
        this.currentCycle = currentCycle;
    }

    public int getCycleStartPlayerCount() {
        return cycleStartPlayerCount;
    }

    public void setCycleStartPlayerCount(int cycleStartPlayerCount) {
        this.cycleStartPlayerCount = cycleStartPlayerCount;
    }

    public int getSpeakersCompletedInCycle() {
        return speakersCompletedInCycle;
    }

    public void setSpeakersCompletedInCycle(int speakersCompletedInCycle) {
        this.speakersCompletedInCycle = speakersCompletedInCycle;
    }

    public boolean isProfileStatsApplied() {
        return profileStatsApplied;
    }

    public void setProfileStatsApplied(boolean profileStatsApplied) {
        this.profileStatsApplied = profileStatsApplied;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public Set<ScoreEntry> getScoreEntries() {
        return scoreEntries;
    }

    public void setScoreEntries(Set<ScoreEntry> scoreEntries) {
        this.scoreEntries = scoreEntries;
    }

}