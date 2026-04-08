package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.ChatMessage;
import n7.projet.repository.ChatMessageRepository;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage createMessage(ChatMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }

    public ChatMessage getMessageById(Long id) {
        return chatMessageRepository.findById(id).orElse(null);
    }

    public List<ChatMessage> getMessagesByRoundId(Long roundId) {
        return chatMessageRepository.findByRoundIdOrderBySentAtAsc(roundId);
    }
}