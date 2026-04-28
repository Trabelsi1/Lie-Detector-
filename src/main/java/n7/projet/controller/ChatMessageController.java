package n7.projet.controller;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import n7.projet.entity.ChatMessage;
import n7.projet.service.ChatMessageService;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> createMessage(@RequestBody ChatMessage message) {
        ChatMessage createdMessage = chatMessageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(createdMessage));
    }

    @GetMapping
    public List<ChatMessageResponse> getAllMessages() {
        return chatMessageService.getAllMessages().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatMessageResponse> getMessageById(@PathVariable Long id) {
        ChatMessage message = chatMessageService.getMessageById(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(message));
    }

    @GetMapping("/round/{roundId}")
    public List<ChatMessageResponse> getMessagesByRoundId(@PathVariable Long roundId) {
        return chatMessageService.getMessagesByRoundId(roundId).stream().map(this::toResponse).toList();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        String senderName = null;
        Long senderId = null;
        if (message.getSender() != null) {
            senderId = message.getSender().getId();
        }
        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                message.getSentAt(),
                senderId,
                senderName);
    }

    public record ChatMessageResponse(Long id, String content, LocalDateTime sentAt, Long senderId, String senderName) {
    }
}