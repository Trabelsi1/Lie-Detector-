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

import n7.projet.entity.GameRoom;
import n7.projet.service.GameRoomService;

@RestController
@RequestMapping("/api/rooms")
public class GameRoomController {

    private final GameRoomService gameRoomService;

    public GameRoomController(GameRoomService gameRoomService) {
        this.gameRoomService = gameRoomService;
    }

    @PostMapping
    public ResponseEntity<GameRoom> createRoom(@RequestBody GameRoom room) {
        GameRoom createdRoom = gameRoomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @GetMapping
    public List<GameRoom> getAllRooms() {
        return gameRoomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameRoom> getRoomById(@PathVariable Long id) {
        GameRoom room = gameRoomService.getRoomById(id);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{roomId}/users/{userId}")
    public ResponseEntity<GameRoom> addUserToRoom(@PathVariable Long roomId, @PathVariable Long userId) {
        GameRoom updatedRoom = gameRoomService.addUserToRoom(roomId, userId);
        if (updatedRoom == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedRoom);
    }
}
