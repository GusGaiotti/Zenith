package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    boolean existsByLedgerIdAndInvitedEmailAndStatus(Long ledgerId, String email, Invitation.InvitationStatus status);
    List<Invitation> findByLedgerIdAndStatusOrderByCreatedAtDesc(Long ledgerId, Invitation.InvitationStatus status);
}
