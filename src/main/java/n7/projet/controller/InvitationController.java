package n7.projet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import n7.projet.entity.Invitation;
import n7.projet.service.InvitationService;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<Invitation> createInvitation(@RequestBody Invitation invitation) {
        Invitation createdInvitation = invitationService.createInvitation(invitation);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvitation);
    }

    @GetMapping
    public List<Invitation> getAllInvitations() {
        return invitationService.getAllInvitations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable Long id) {
        Invitation invitation = invitationService.getInvitationById(id);
        if (invitation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/room/{roomId}")
    public List<Invitation> getInvitationsByRoomId(@PathVariable Long roomId) {
        return invitationService.getInvitationsByRoomId(roomId);
    }

    @GetMapping("/receiver/{receiverId}")
    public List<Invitation> getInvitationsByReceiverId(@PathVariable Long receiverId) {
        return invitationService.getInvitationsByReceiverId(receiverId);
    }
}