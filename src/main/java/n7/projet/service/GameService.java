package n7.projet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.Game;
import n7.projet.repository.GameRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    public List<Game> getGamesByRoomId(Long roomId) {
        return gameRepository.findByGameRoomId(roomId);
    }
}