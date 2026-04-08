package n7.projet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        room.setPlayers(new HashSet<>());

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
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(playerRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
}