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

import n7.projet.entity.PlayerProfile;
import n7.projet.service.PlayerProfileService;

@RestController
@RequestMapping("/api/profiles")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @PostMapping
    public ResponseEntity<PlayerProfile> createProfile(@RequestBody PlayerProfile profile) {
        PlayerProfile createdProfile = playerProfileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @GetMapping
    public List<PlayerProfile> getAllProfiles() {
        return playerProfileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerProfile> getProfileById(@PathVariable Long id) {
        PlayerProfile profile = playerProfileService.getProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<PlayerProfile> getProfileByPlayerId(@PathVariable Long playerId) {
        PlayerProfile profile = playerProfileService.getProfileByPlayerId(playerId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }
}