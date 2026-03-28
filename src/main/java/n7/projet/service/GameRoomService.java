package n7.projet.service;

import n7.projet.entity.GameRoom;
import n7.projet.entity.User;
import n7.projet.repository.GameRoomRepository;
import n7.projet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;

    public GameRoomService(GameRoomRepository gameRoomRepository, UserRepository userRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.userRepository = userRepository;
    }

    public GameRoom createRoom(GameRoom room) {
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

        room.getUsers().add(user);
        user.getRooms().add(room);

        return gameRoomRepository.save(room);
    }
}
