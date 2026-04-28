package n7.projet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.GameRoom;
import n7.projet.entity.Player;
import n7.projet.entity.User;
import n7.projet.repository.GameRoomRepository;
import n7.projet.repository.PlayerRepository;
import n7.projet.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GameRoomServiceTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameRoomService gameRoomService;

    private GameRoom room;
    private User user;

    @BeforeEach
    void setUp() {
        room = new GameRoom();
        room.setRoomCode("ROOM-1");
        room.setStatus("OPEN");
        room.setPlayers(new ArrayList<>());

        user = new User();
        user.setId(2L);
        user.setUsername("bob");
        user.setEmail("bob@example.com");
    }

    @Test
    void createRoomShouldSetCreatedAtWhenMissing() {
        when(gameRoomRepository.save(any(GameRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameRoom createdRoom = gameRoomService.createRoom(room);

        assertNotNull(createdRoom.getCreatedAt());
        assertEquals("ROOM-1", createdRoom.getRoomCode());
        verify(gameRoomRepository).save(room);
    }

    @Test
    void addUserToRoomShouldCreatePlayerAndLinkBothSides() {
        AtomicReference<Player> savedPlayerRef = new AtomicReference<>();

        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(playerRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player savedPlayer = invocation.getArgument(0);
            savedPlayer.setId(3L);
            savedPlayerRef.set(savedPlayer);
            return savedPlayer;
        });
        when(playerRepository.findById(3L)).thenAnswer(invocation -> Optional.of(savedPlayerRef.get()));
        when(gameRoomRepository.save(any(GameRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameRoom updatedRoom = gameRoomService.addUserToRoom(1L, 2L);

        assertNotNull(updatedRoom);
        assertEquals(1, updatedRoom.getPlayers().size());

        Player createdPlayer = updatedRoom.getPlayers().iterator().next();
        assertEquals(user, createdPlayer.getUser());
        assertEquals(1, createdPlayer.getRooms().size());
        assertEquals(updatedRoom, createdPlayer.getRooms().iterator().next());
        assertNotNull(createdPlayer.getProfile());
        assertEquals(createdPlayer, createdPlayer.getProfile().getPlayer());
        assertEquals(createdPlayer, user.getPlayer());
    }

    @Test
    void addUserToRoomShouldReturnNullWhenRoomOrUserIsMissing() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.empty());

        GameRoom updatedRoom = gameRoomService.addUserToRoom(1L, 2L);

        assertNull(updatedRoom);
    }

    @Test
    void joinPlayerToRoomShouldRejectFullRooms() {
        room.setId(1L);
        room.setMaxPlayers(1);

        Player existingPlayer = new Player();
        existingPlayer.setId(10L);
        room.getPlayers().add(existingPlayer);

        Player joiningPlayer = new Player();
        joiningPlayer.setId(11L);

        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(joiningPlayer));

        assertThrows(ResponseStatusException.class, () -> gameRoomService.joinPlayerToRoom(1L, 11L));
    }
}