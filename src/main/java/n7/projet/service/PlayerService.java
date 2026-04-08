package n7.projet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.Player;
import n7.projet.entity.PlayerProfile;
import n7.projet.entity.User;
import n7.projet.repository.PlayerRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(Player player) {
        User user = player.getUser();
        if (user != null && user.getPlayer() != player) {
            user.setPlayer(player);
        }

        PlayerProfile profile = player.getProfile();
        if (profile != null && profile.getPlayer() != player) {
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