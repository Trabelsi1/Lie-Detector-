package n7.projet.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.Player;
import n7.projet.entity.PlayerProfile;
import n7.projet.entity.User;
import n7.projet.repository.PlayerRepository;
import n7.projet.repository.UserRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PlayerService(PlayerRepository playerRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    public Player createPlayer(Player player) {
        User user = player.getUser();
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A player must be attached to an existing user");
        }

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Player existingPlayer = playerRepository.findByUserId(managedUser.getId()).orElse(null);
        if (existingPlayer != null) {
            return existingPlayer;
        }

        player.setUser(managedUser);
        managedUser.setPlayer(player);

        PlayerProfile profile = player.getProfile();
        if (profile == null) {
            profile = new PlayerProfile();
            player.setProfile(profile);
        }

        if (profile.getPlayer() != player) {
            profile.setPlayer(player);
        }

        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    public Player getPlayerByUserId(Long userId) {
        return playerRepository.findByUserId(userId).orElse(null);
    }
}