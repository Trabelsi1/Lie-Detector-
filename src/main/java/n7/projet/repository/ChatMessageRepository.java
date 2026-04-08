package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoundIdOrderBySentAtAsc(Long roundId);
}