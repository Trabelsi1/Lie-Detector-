package n7.projet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import n7.projet.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByGameRoomId(Long gameRoomId);

    List<Invitation> findByReceiverId(Long receiverId);
}