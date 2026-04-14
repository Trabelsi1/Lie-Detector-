package n7.projet.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
@JsonIgnoreProperties({ "rooms", "invitationsSent", "invitationsReceived", "votes", "chatMessages", "scoreEntries" })
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnoreProperties({ "player" })
    private User user;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerProfile profile;

    @ManyToMany(mappedBy = "players")
    private Set<GameRoom> rooms = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Invitation> invitationsSent = new HashSet<>();

    @OneToMany(mappedBy = "receiver")
    private Set<Invitation> invitationsReceived = new HashSet<>();

    @OneToMany(mappedBy = "voter")
    private Set<Vote> votes = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<ChatMessage> chatMessages = new HashSet<>();

    @OneToMany(mappedBy = "player")
    private Set<ScoreEntry> scoreEntries = new HashSet<>();

    public Player() {
    }

    public Player(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }

    public Set<GameRoom> getRooms() {
        return rooms;
    }

    public void setRooms(Set<GameRoom> rooms) {
        this.rooms = rooms;
    }

    public Set<Invitation> getInvitationsSent() {
        return invitationsSent;
    }

    public void setInvitationsSent(Set<Invitation> invitationsSent) {
        this.invitationsSent = invitationsSent;
    }

    public Set<Invitation> getInvitationsReceived() {
        return invitationsReceived;
    }

    public void setInvitationsReceived(Set<Invitation> invitationsReceived) {
        this.invitationsReceived = invitationsReceived;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public Set<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(Set<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public Set<ScoreEntry> getScoreEntries() {
        return scoreEntries;
    }

    public void setScoreEntries(Set<ScoreEntry> scoreEntries) {
        this.scoreEntries = scoreEntries;
    }
}
