package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import n7.projet.entity.GameRoom;
import n7.projet.entity.Player;
import n7.projet.entity.PlayerProfile;
import n7.projet.entity.User;
import n7.projet.repository.GameRoomRepository;
import n7.projet.repository.PlayerRepository;
import n7.projet.repository.UserRepository;

@Service
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public GameRoomService(GameRoomRepository gameRoomRepository, UserRepository userRepository,
            PlayerRepository playerRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public GameRoom createRoom(GameRoom room) {
        if (room.getCreatedAt() == null) {
            room.setCreatedAt(LocalDateTime.now());
        }
        return gameRoomRepository.save(room);
    }

    public List<GameRoom> getAllRooms() {
        return gameRoomRepository.findAll();
    }

    public GameRoom getRoomById(Long id) {
        return gameRoomRepository.findById(id).orElse(null);
    }

    public GameRoom addUserToRoom(Long roomId, Long userId) {
        Optional<GameRoom> roomOptional = gameRoomRepository.findById(roomId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (roomOptional.isEmpty() || userOptional.isEmpty()) {
            return null;
        }

        GameRoom room = roomOptional.get();
        User user = userOptional.get();
        Player player = getOrCreatePlayer(user);

        room.getPlayers().add(player);
        player.getRooms().add(room);

        return gameRoomRepository.save(room);
    }

    private Player getOrCreatePlayer(User user) {
        return playerRepository.findByUserId(user.getId()).orElseGet(() -> {
            Player player = new Player(user);
            PlayerProfile profile = new PlayerProfile();
            profile.setPlayer(player);
            player.setProfile(profile);
            user.setPlayer(player);
            return playerRepository.save(player);
        });
    }
}
