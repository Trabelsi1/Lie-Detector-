package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByGameRoomId(Long gameRoomId);

    List<Game> findByGameRoomIdOrderByIdDesc(Long gameRoomId);
}