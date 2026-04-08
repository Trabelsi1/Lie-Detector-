package n7.projet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import n7.projet.entity.ChatMessage;
import n7.projet.entity.Game;
import n7.projet.entity.GameRoom;
import n7.projet.entity.Invitation;
import n7.projet.entity.Player;
import n7.projet.entity.PlayerProfile;
import n7.projet.entity.Round;
import n7.projet.entity.ScoreEntry;
import n7.projet.entity.Statement;
import n7.projet.entity.User;
import n7.projet.entity.Vote;
import n7.projet.service.ChatMessageService;
import n7.projet.service.GameRoomService;
import n7.projet.service.GameService;
import n7.projet.service.InvitationService;
import n7.projet.service.PlayerProfileService;
import n7.projet.service.PlayerService;
import n7.projet.service.RoundService;
import n7.projet.service.ScoreEntryService;
import n7.projet.service.StatementService;
import n7.projet.service.UserService;
import n7.projet.service.VoteService;

@WebMvcTest({ UserController.class, GameRoomController.class, GameController.class, RoundController.class,
        StatementController.class, VoteController.class, ChatMessageController.class, InvitationController.class,
        ScoreEntryController.class, PlayerController.class, PlayerProfileController.class })
@Import(BackendControllersTest.MockConfig.class)
class BackendControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private GameService gameService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private StatementService statementService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private ScoreEntryService scoreEntryService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerProfileService playerProfileService;

    @TestConfiguration
    static class MockConfig {

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        GameRoomService gameRoomService() {
            return mock(GameRoomService.class);
        }

        @Bean
        GameService gameService() {
            return mock(GameService.class);
        }

        @Bean
        RoundService roundService() {
            return mock(RoundService.class);
        }

        @Bean
        StatementService statementService() {
            return mock(StatementService.class);
        }

        @Bean
        VoteService voteService() {
            return mock(VoteService.class);
        }

        @Bean
        ChatMessageService chatMessageService() {
            return mock(ChatMessageService.class);
        }

        @Bean
        InvitationService invitationService() {
            return mock(InvitationService.class);
        }

        @Bean
        ScoreEntryService scoreEntryService() {
            return mock(ScoreEntryService.class);
        }

        @Bean
        PlayerService playerService() {
            return mock(PlayerService.class);
        }

        @Bean
        PlayerProfileService playerProfileService() {
            return mock(PlayerProfileService.class);
        }
    }

    @Test
    void shouldReturnUsersList() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setCreatedAt(LocalDateTime.now());

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void shouldCreateRoom() throws Exception {
        GameRoom room = new GameRoom();
        room.setId(1L);
        room.setRoomCode("ROOM-1");
        room.setStatus("OPEN");
        room.setMaxPlayers(4);
        room.setCreatedAt(LocalDateTime.now());

        when(gameRoomService.createRoom(any(GameRoom.class))).thenReturn(room);

        mockMvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"roomCode\":\"ROOM-1\"," +
                        "\"status\":\"OPEN\"," +
                        "\"maxPlayers\":4" +
                        "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomCode").value("ROOM-1"));
    }

    @Test
    void shouldExposeGameEndpoints() throws Exception {
        Game game = new Game();
        game.setId(10L);
        game.setStatus("STARTED");
        when(gameService.getAllGames()).thenReturn(List.of(game));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("STARTED"));
    }

    @Test
    void shouldCreateVoteFromPathParameters() throws Exception {
        Vote vote = new Vote();
        vote.setId(1L);

        when(voteService.createVote(1L, 2L, 3L)).thenReturn(vote);

        mockMvc.perform(post("/api/votes/round/1/voter/2/statement/3"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldExposePlayerAndProfileEndpoints() throws Exception {
        Player player = new Player();
        player.setId(7L);
        User user = new User();
        user.setId(8L);
        player.setUser(user);
        when(playerService.getPlayerByUserId(8L)).thenReturn(player);

        PlayerProfile profile = new PlayerProfile();
        profile.setId(9L);
        when(playerProfileService.getProfileByPlayerId(7L)).thenReturn(profile);

        mockMvc.perform(get("/api/players/user/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        mockMvc.perform(get("/api/profiles/player/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void shouldExposeRoundStatementChatInvitationAndScoreEndpoints() throws Exception {
        Round round = new Round();
        round.setId(11L);
        when(roundService.getRoundsByGameId(1L)).thenReturn(List.of(round));

        Statement statement = new Statement();
        statement.setId(12L);
        when(statementService.getStatementsByRoundId(11L)).thenReturn(List.of(statement));

        ChatMessage message = new ChatMessage();
        message.setId(13L);
        when(chatMessageService.getMessagesByRoundId(11L)).thenReturn(List.of(message));

        Invitation invitation = new Invitation();
        invitation.setId(14L);
        when(invitationService.getInvitationsByReceiverId(15L)).thenReturn(List.of(invitation));

        ScoreEntry scoreEntry = new ScoreEntry();
        scoreEntry.setId(16L);
        when(scoreEntryService.getScoreEntriesByPlayerId(17L)).thenReturn(List.of(scoreEntry));

        mockMvc.perform(get("/api/rounds/game/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));

        mockMvc.perform(get("/api/statements/round/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12));

        mockMvc.perform(get("/api/messages/round/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(13));

        mockMvc.perform(get("/api/invitations/receiver/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(14));

        mockMvc.perform(get("/api/scores/player/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(16));
    }
}