package n7.projet.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "users")
    private Set<GameRoom> rooms = new HashSet<>();

    @OneToOne(mappedBy = "user")
    private PlayerProfile profile;

    @OneToMany(mappedBy = "sender")
    private Set<Invitation> invitationsSent = new HashSet<>();

    @OneToMany(mappedBy = "receiver")
    private Set<Invitation> invitationsReceived = new HashSet<>();

    @OneToMany(mappedBy = "voter")
    private Set<Vote> votes = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<ChatMessage> chatMessages = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ScoreEntry> scoreEntries = new HashSet<>();

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<GameRoom> getRooms() {
        return rooms;
    }

    public void setRooms(Set<GameRoom> rooms) {
        this.rooms = rooms;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }
}
