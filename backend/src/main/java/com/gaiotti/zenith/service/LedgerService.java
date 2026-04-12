package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.InvitationResponse;
import com.gaiotti.zenith.dto.response.LedgerResponse;
import com.gaiotti.zenith.dto.response.MemberResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.LedgerFullException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Invitation;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.LedgerMember;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.InvitationRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private static final int MAX_MEMBERS = 2;
    private static final int MAX_LEDGER_NAME_LENGTH = 120;

    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public LedgerResponse createLedger(String name, User authenticatedUser) {
        if (ledgerMemberRepository.existsByUserId(authenticatedUser.getId())) {
            throw new IllegalArgumentException("User already belongs to a ledger");
        }

        String normalizedName = normalizeLedgerName(name);

        Ledger ledger = Ledger.builder()
                .name(normalizedName)
                .build();
        ledger = ledgerRepository.save(ledger);

        LedgerMember member = LedgerMember.builder()
                .ledger(ledger)
                .user(authenticatedUser)
                .build();
        ledgerMemberRepository.save(member);

        return buildLedgerResponse(ledger);
    }

    @Transactional
    public InvitationResponse inviteUser(Long ledgerId, User inviter, String targetEmail) {
        Ledger ledger = ledgerRepository.findByIdWithLock(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, inviter.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }

        long memberCount = ledgerMemberRepository.countByLedgerId(ledgerId);
        if (memberCount >= MAX_MEMBERS) {
            throw new LedgerFullException("Ledger already has the maximum of 2 members");
        }

        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No registered user with that email"));

        if (ledgerMemberRepository.existsByUserId(targetUser.getId())) {
            throw new IllegalArgumentException("User already belongs to another ledger");
        }

        if (ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, targetUser.getId())) {
            throw new IllegalArgumentException("User is already a member of this ledger");
        }

        if (invitationRepository.existsByLedgerIdAndInvitedEmailAndStatus(
                ledgerId, targetEmail, Invitation.InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("A pending invitation already exists for that email");
        }

        Invitation invitation = Invitation.builder()
                .ledger(ledger)
                .invitedBy(inviter)
                .invitedEmail(targetEmail)
                .build();
        invitation = invitationRepository.save(invitation);
        notificationService.createInvitationNotification(invitation, targetUser);

        return buildInvitationResponse(invitation);
    }

    @Transactional
    public LedgerResponse acceptInvitation(String token, User authenticatedUser) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }

        if (!invitation.getInvitedEmail().equals(authenticatedUser.getEmail())) {
            throw new AccessDeniedException("You are not the intended recipient of this invitation");
        }

        if (ledgerMemberRepository.existsByUserId(authenticatedUser.getId())) {
            throw new IllegalArgumentException("User already belongs to a ledger");
        }

        Long ledgerId = invitation.getLedger().getId();
        
        ledgerRepository.findByIdWithLock(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));
        
        long memberCount = ledgerMemberRepository.countByLedgerId(ledgerId);
        if (memberCount >= MAX_MEMBERS) {
            throw new LedgerFullException("Ledger already has the maximum of 2 members");
        }

        LedgerMember member = LedgerMember.builder()
                .ledger(invitation.getLedger())
                .user(authenticatedUser)
                .build();
        ledgerMemberRepository.save(member);

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        return buildLedgerResponse(invitation.getLedger());
    }

    @Transactional
    public InvitationResponse declineInvitation(String token, User authenticatedUser) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        if (!invitation.getInvitedEmail().equals(authenticatedUser.getEmail())) {
            throw new AccessDeniedException("You are not the intended recipient of this invitation");
        }

        invitation.setStatus(Invitation.InvitationStatus.DECLINED);
        invitationRepository.save(invitation);

        return buildInvitationResponse(invitation);
    }

    @Transactional
    public InvitationResponse cancelInvitation(String token, User authenticatedUser) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        if (!invitation.getInvitedBy().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Only the inviter can cancel this invitation");
        }

        invitation.setStatus(Invitation.InvitationStatus.CANCELED);
        invitationRepository.save(invitation);

        return buildInvitationResponse(invitation);
    }

    @Transactional(readOnly = true)
    public LedgerResponse getLedgerDetails(Long ledgerId, User authenticatedUser) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, authenticatedUser.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }

        return buildLedgerResponse(ledger);
    }

    @Transactional
    public LedgerResponse updateLedgerName(Long ledgerId, String name, User authenticatedUser) {
        String normalizedName = normalizeLedgerName(name);

        Ledger ledger = ledgerRepository.findByIdWithLock(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, authenticatedUser.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }

        ledger.setName(normalizedName);
        ledgerRepository.save(ledger);
        return buildLedgerResponse(ledger);
    }

    @Transactional(readOnly = true)
    public Optional<LedgerResponse> getCurrentUserLedger(User authenticatedUser) {
        return ledgerMemberRepository.findFirstByUserId(authenticatedUser.getId())
                .map(LedgerMember::getLedger)
                .map(this::buildLedgerResponse);
    }

    private LedgerResponse buildLedgerResponse(Ledger ledger) {
        List<LedgerMember> members = ledgerMemberRepository.findByLedgerId(ledger.getId());
        List<MemberResponse> memberResponses = members.stream()
                .map(m -> MemberResponse.builder()
                        .userId(m.getUser().getId())
                        .email(m.getUser().getEmail())
                        .displayName(m.getUser().getDisplayName())
                        .joinedAt(m.getJoinedAt())
                .build())
                .toList();

        List<InvitationResponse> pendingInvitations = invitationRepository
                .findByLedgerIdAndStatusOrderByCreatedAtDesc(ledger.getId(), Invitation.InvitationStatus.PENDING)
                .stream()
                .map(this::buildInvitationResponse)
                .toList();

        return LedgerResponse.builder()
                .id(ledger.getId())
                .name(ledger.getName())
                .createdAt(ledger.getCreatedAt())
                .members(memberResponses)
                .pendingInvitations(pendingInvitations)
                .build();
    }

    private InvitationResponse buildInvitationResponse(Invitation invitation) {
        User invitedUser = userRepository.findByEmail(invitation.getInvitedEmail()).orElse(null);

        return InvitationResponse.builder()
                .id(invitation.getId())
                .token(invitation.getToken())
                .invitedEmail(invitation.getInvitedEmail())
                .invitedUserDisplayName(invitedUser != null ? invitedUser.getDisplayName() : null)
                .invitedByDisplayName(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getDisplayName() : null)
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    private String normalizeLedgerName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Ledger name is required");
        }

        String normalized = name.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Ledger name is required");
        }
        if (normalized.length() > MAX_LEDGER_NAME_LENGTH) {
            throw new IllegalArgumentException("Ledger name must have at most 120 characters");
        }

        return normalized;
    }
}
