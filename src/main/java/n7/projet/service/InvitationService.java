package n7.projet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import n7.projet.entity.Invitation;
import n7.projet.repository.InvitationRepository;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;

    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public Invitation createInvitation(Invitation invitation) {
        if (invitation.getSentAt() == null) {
            invitation.setSentAt(LocalDateTime.now());
        }
        if (invitation.getStatus() == null || invitation.getStatus().isBlank()) {
            invitation.setStatus("PENDING");
        }
        return invitationRepository.save(invitation);
    }

    public List<Invitation> getAllInvitations() {
        return invitationRepository.findAll();
    }

    public Invitation getInvitationById(Long id) {
        return invitationRepository.findById(id).orElse(null);
    }

    public List<Invitation> getInvitationsByRoomId(Long roomId) {
        return invitationRepository.findByGameRoomId(roomId);
    }

    public List<Invitation> getInvitationsByReceiverId(Long receiverId) {
        return invitationRepository.findByReceiverId(receiverId);
    }
}