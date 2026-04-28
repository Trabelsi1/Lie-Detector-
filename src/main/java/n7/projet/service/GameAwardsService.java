package n7.projet.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import n7.projet.entity.ChatMessage;
import n7.projet.entity.Game;
import n7.projet.entity.Player;
import n7.projet.entity.PlayerProfile;
import n7.projet.entity.Round;
import n7.projet.entity.ScoreEntry;
import n7.projet.entity.Statement;
import n7.projet.entity.Vote;
import n7.projet.repository.ChatMessageRepository;
import n7.projet.repository.GameRepository;
import n7.projet.repository.PlayerProfileRepository;
import n7.projet.repository.RoundRepository;
import n7.projet.repository.ScoreEntryRepository;
import n7.projet.repository.StatementRepository;
import n7.projet.repository.VoteRepository;

@Service
public class GameAwardsService {

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final VoteRepository voteRepository;
    private final StatementRepository statementRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ScoreEntryRepository scoreEntryRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public GameAwardsService(GameRepository gameRepository, RoundRepository roundRepository,
            VoteRepository voteRepository, StatementRepository statementRepository,
            ChatMessageRepository chatMessageRepository, ScoreEntryRepository scoreEntryRepository,
            PlayerProfileRepository playerProfileRepository) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.voteRepository = voteRepository;
        this.statementRepository = statementRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.scoreEntryRepository = scoreEntryRepository;
        this.playerProfileRepository = playerProfileRepository;
    }

    @Transactional
    public void applyLifetimeStatsIfCompleted(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (!"COMPLETED".equalsIgnoreCase(String.valueOf(game.getStatus()))) {
            return;
        }
        if (game.isProfileStatsApplied()) {
            return;
        }

        List<Player> players = game.getGameRoom() != null && game.getGameRoom().getPlayers() != null
                ? game.getGameRoom().getPlayers()
                : List.of();

        if (players.isEmpty()) {
            game.setProfileStatsApplied(true);
            gameRepository.save(game);
            return;
        }

        List<ScoreEntry> entries = scoreEntryRepository.findByGameId(gameId);
        Map<Long, Integer> pointsByPlayer = new HashMap<>();
        Map<Long, Integer> fooledByPlayer = new HashMap<>();
        for (Player player : players) {
            pointsByPlayer.put(player.getId(), 0);
            fooledByPlayer.put(player.getId(), 0);
        }

        for (ScoreEntry entry : entries) {
            if (entry.getPlayer() == null || entry.getPlayer().getId() == null) {
                continue;
            }
            Long playerId = entry.getPlayer().getId();
            pointsByPlayer.put(playerId, pointsByPlayer.getOrDefault(playerId, 0) + entry.getPoints());
            fooledByPlayer.put(playerId, fooledByPlayer.getOrDefault(playerId, 0) + entry.getPlayersFooled());
        }

        int topPoints = pointsByPlayer.values().stream().max(Integer::compareTo).orElse(0);
        Set<Long> winnerIds = new HashSet<>();
        for (Map.Entry<Long, Integer> entry : pointsByPlayer.entrySet()) {
            if (entry.getValue() == topPoints) {
                winnerIds.add(entry.getKey());
            }
        }

        for (Player player : players) {
            PlayerProfile profile = playerProfileRepository.findByPlayerId(player.getId()).orElseGet(() -> {
                PlayerProfile p = new PlayerProfile();
                p.setPlayer(player);
                p.setArchetype("Balanced");
                p.setDeceptionRate(0.0);
                p.setDetectionRate(0.0);
                p.setTotalGames(0);
                p.setTotalWins(0);
                p.setTotalLosses(0);
                p.setTotalPlayersFooled(0);
                return p;
            });

            profile.setTotalGames(profile.getTotalGames() + 1);
            if (winnerIds.contains(player.getId())) {
                profile.setTotalWins(profile.getTotalWins() + 1);
            } else {
                profile.setTotalLosses(profile.getTotalLosses() + 1);
            }
            profile.setTotalPlayersFooled(profile.getTotalPlayersFooled()
                    + fooledByPlayer.getOrDefault(player.getId(), 0));

            playerProfileRepository.save(profile);
        }

        game.setProfileStatsApplied(true);
        if (game.getEndTime() == null) {
            game.setEndTime(LocalDateTime.now());
        }
        gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public FinalSummary getFinalSummary(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Player> players = game.getGameRoom() != null && game.getGameRoom().getPlayers() != null
                ? game.getGameRoom().getPlayers()
                : List.of();

        Map<Long, PlayerStats> stats = new HashMap<>();
        for (Player player : players) {
            if (player == null || player.getId() == null) {
                continue;
            }
            stats.put(player.getId(), new PlayerStats(player.getId(),
                    player.getUser() != null ? player.getUser().getUsername() : "Unknown"));
        }

        List<Round> rounds = roundRepository.findByGameIdOrderByRoundNumberAsc(gameId);
        for (Round round : rounds) {
            if (round == null || round.getId() == null) {
                continue;
            }

            if (round.getSpeakerId() != null && stats.containsKey(round.getSpeakerId())
                    && "RESULTS".equals(round.getPhase())) {
                stats.get(round.getSpeakerId()).speakerRounds += 1;
            }

            List<Statement> statements = statementRepository.findByRoundId(round.getId());
            Long lieStatementId = statements.stream().filter(Statement::isLie).map(Statement::getId).findFirst().orElse(null);

            List<Vote> votes = voteRepository.findByRoundId(round.getId());
            for (Vote vote : votes) {
                if (vote.getVoter() == null || vote.getVoter().getId() == null) {
                    continue;
                }
                PlayerStats voterStats = stats.get(vote.getVoter().getId());
                if (voterStats == null) {
                    continue;
                }
                voterStats.totalVotes += 1;
                if (lieStatementId != null && vote.getStatement() != null && lieStatementId.equals(vote.getStatement().getId())) {
                    voterStats.correctVotes += 1;
                } else {
                    voterStats.wrongVotes += 1;
                }
            }

            List<ChatMessage> messages = chatMessageRepository.findByRoundIdOrderBySentAtAsc(round.getId());
            for (ChatMessage message : messages) {
                if (message.getSender() == null || message.getSender().getId() == null) {
                    continue;
                }
                PlayerStats senderStats = stats.get(message.getSender().getId());
                if (senderStats != null) {
                    senderStats.messages += 1;
                }
            }
        }

        List<ScoreEntry> entries = scoreEntryRepository.findByGameId(gameId);
        for (ScoreEntry entry : entries) {
            if (entry.getPlayer() == null || entry.getPlayer().getId() == null) {
                continue;
            }
            PlayerStats playerStats = stats.get(entry.getPlayer().getId());
            if (playerStats == null) {
                continue;
            }
            playerStats.points += entry.getPoints();
            playerStats.playersFooled += entry.getPlayersFooled();
            playerStats.correctVotes += entry.getCorrectGuesses();
        }

        List<PlayerStats> statList = new ArrayList<>(stats.values());
        List<GameService.PlayerScore> rankings = statList.stream()
                .sorted(Comparator.comparingInt((PlayerStats p) -> p.points).reversed())
                .map(p -> new GameService.PlayerScore(p.playerId, p.playerName, p.points))
                .toList();

        List<Award> awards = new ArrayList<>();
        awards.add(buildAward("MOST_FOOLED", "Most Fooled", statList,
                Comparator.comparingInt((PlayerStats p) -> p.wrongVotes).reversed(), false, true,
                p -> String.valueOf(p.wrongVotes)));

        awards.add(buildAward("BEST_LIAR_TOTAL", "Best Liar (Total)", statList,
                Comparator.comparingInt((PlayerStats p) -> p.playersFooled).reversed(), false, true,
                p -> String.valueOf(p.playersFooled)));

        awards.add(buildAward("BEST_LIAR_AVG", "Best Liar (Average)", statList,
                Comparator.comparingDouble(PlayerStats::liarAverage).reversed(), true, true,
                p -> String.format("%.2f", p.liarAverage())));

        awards.add(buildAward("MOST_GULLIBLE", "Most Gullible", statList,
                Comparator.comparingDouble(PlayerStats::wrongRate).reversed(), true, true,
                p -> String.format("%.2f%%", p.wrongRate() * 100)));

        awards.add(buildAward("BEST_DETECTIVE", "Best Detective", statList,
                Comparator.comparingInt((PlayerStats p) -> p.correctVotes).reversed(), false, true,
                p -> String.valueOf(p.correctVotes)));

        awards.add(buildAward("MOST_ACCURATE", "Most Accurate", statList,
                Comparator.comparingDouble(PlayerStats::correctRate).reversed(), true, true,
                p -> String.format("%.2f%%", p.correctRate() * 100)));

        awards.add(buildAward("MOST_ACTIVE", "Most Active", statList,
                Comparator.comparingInt((PlayerStats p) -> p.messages).reversed(), false, true,
                p -> String.valueOf(p.messages)));

        awards.add(buildAward("MOST_SILENT", "Most Silent", statList,
                Comparator.comparingInt((PlayerStats p) -> p.messages), false, true,
                p -> String.valueOf(p.messages)));

        return new FinalSummary(rankings, awards);
    }

    private Award buildAward(String key, String title, List<PlayerStats> players,
            Comparator<PlayerStats> primaryComparator, boolean requireVotes, boolean tieBreakByLiar,
            java.util.function.Function<PlayerStats, String> valueFormatter) {

        List<PlayerStats> eligible = players.stream()
                .filter(p -> !requireVotes || p.totalVotes > 0)
                .toList();

        if (eligible.isEmpty()) {
            return new Award(key, title, List.of(), false);
        }

        List<PlayerStats> sorted = new ArrayList<>(eligible);
        sorted.sort((a, b) -> {
            int c = primaryComparator.compare(a, b);
            if (c != 0) {
                return c;
            }

            if (tieBreakByLiar) {
                int liarTotalCmp = Integer.compare(b.playersFooled, a.playersFooled);
                if (liarTotalCmp != 0) {
                    return liarTotalCmp;
                }
                int liarAvgCmp = Double.compare(b.liarAverage(), a.liarAverage());
                if (liarAvgCmp != 0) {
                    return liarAvgCmp;
                }
            }

            return 0;
        });

        PlayerStats winner = sorted.get(0);
        List<Winner> winners = new ArrayList<>();
        winners.add(new Winner(winner.playerId, winner.playerName, valueFormatter.apply(winner)));

        boolean tie = false;
        for (int i = 1; i < sorted.size(); i++) {
            PlayerStats contender = sorted.get(i);
            if (primaryComparator.compare(winner, contender) != 0) {
                break;
            }

            if (tieBreakByLiar) {
                boolean liarExactlyEqual = winner.playersFooled == contender.playersFooled
                        && Double.compare(winner.liarAverage(), contender.liarAverage()) == 0;
                if (!liarExactlyEqual) {
                    continue;
                }
            }

            tie = true;
            winners.add(new Winner(contender.playerId, contender.playerName, valueFormatter.apply(contender)));
        }

        return new Award(key, title, winners, tie);
    }

    private static class PlayerStats {
        final Long playerId;
        final String playerName;
        int points;
        int correctVotes;
        int wrongVotes;
        int totalVotes;
        int messages;
        int playersFooled;
        int speakerRounds;

        PlayerStats(Long playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
        }

        double liarAverage() {
            return speakerRounds > 0 ? ((double) playersFooled) / speakerRounds : 0.0;
        }

        double wrongRate() {
            return totalVotes > 0 ? ((double) wrongVotes) / totalVotes : 0.0;
        }

        double correctRate() {
            return totalVotes > 0 ? ((double) correctVotes) / totalVotes : 0.0;
        }
    }

    public record Winner(Long playerId, String playerName, String value) {
    }

    public record Award(String key, String title, List<Winner> winners, boolean tie) {
    }

    public record FinalSummary(List<GameService.PlayerScore> rankings, List<Award> awards) {
    }
}
