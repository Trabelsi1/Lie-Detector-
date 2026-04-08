package n7.projet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.PlayerProfile;
import n7.projet.repository.PlayerProfileRepository;

@Service
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository) {
        this.playerProfileRepository = playerProfileRepository;
    }

    public PlayerProfile createProfile(PlayerProfile profile) {
        return playerProfileRepository.save(profile);
    }

    public List<PlayerProfile> getAllProfiles() {
        return playerProfileRepository.findAll();
    }

    public PlayerProfile getProfileById(Long id) {
        return playerProfileRepository.findById(id).orElse(null);
    }

    public PlayerProfile getProfileByPlayerId(Long playerId) {
        return playerProfileRepository.findByPlayerId(playerId).orElse(null);
    }
}