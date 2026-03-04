package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.InvitationResponse;
import com.gaiotti.zenith.dto.response.LedgerResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LedgerService ledgerService;

    private User testUser;
    private User targetUser;
    private Ledger testLedger;
    private LedgerMember testMember;
    private Invitation testInvitation;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        targetUser = User.builder()
                .id(2L)
                .email("target@example.com")
                .displayName("Target User")
                .build();

        testLedger = Ledger.builder()
                .id(1L)
                .name("Test Ledger")
                .createdAt(LocalDateTime.now())
                .build();

        testMember = LedgerMember.builder()
                .id(1L)
                .ledger(testLedger)
                .user(testUser)
                .joinedAt(LocalDateTime.now())
                .build();

        testInvitation = Invitation.builder()
                .id(1L)
                .ledger(testLedger)
                .invitedBy(testUser)
                .invitedEmail(targetUser.getEmail())
                .token("test-token")
                .status(Invitation.InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void createLedger_Success() {
        when(ledgerRepository.save(any(Ledger.class))).thenReturn(testLedger);
        when(ledgerMemberRepository.save(any(LedgerMember.class))).thenReturn(testMember);
        when(ledgerMemberRepository.findByLedgerId(testLedger.getId())).thenReturn(List.of(testMember));

        LedgerResponse response = ledgerService.createLedger("Test Ledger", testUser);

        assertNotNull(response);
        assertEquals(testLedger.getId(), response.getId());
        assertEquals(testLedger.getName(), response.getName());
        assertEquals(1, response.getMembers().size());
        verify(ledgerRepository).save(any(Ledger.class));
        verify(ledgerMemberRepository).save(any(LedgerMember.class));
    }

    @Test
    void inviteUser_Success() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(1L);
        when(userRepository.findByEmail(targetUser.getEmail())).thenReturn(Optional.of(targetUser));
        when(invitationRepository.existsByLedgerIdAndInvitedEmailAndStatus(
                testLedger.getId(), targetUser.getEmail(), Invitation.InvitationStatus.PENDING)).thenReturn(false);
        when(ledgerRepository.findById(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);

        InvitationResponse response = ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail());

        assertNotNull(response);
        assertEquals(testInvitation.getToken(), response.getToken());
        assertEquals(targetUser.getEmail(), response.getInvitedEmail());
        assertEquals(Invitation.InvitationStatus.PENDING.name(), response.getStatus());
        verify(notificationService).createInvitationNotification(any(Invitation.class), eq(targetUser));
    }

    @Test
    void inviteUser_LedgerFull_ThrowsLedgerFullException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(2L);

        assertThrows(LedgerFullException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void inviteUser_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void inviteUser_UserNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(1L);
        when(userRepository.findByEmail(targetUser.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void acceptInvitation_Success() {
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(1L);
        when(ledgerMemberRepository.save(any(LedgerMember.class))).thenReturn(testMember);
        when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
        when(ledgerMemberRepository.findByLedgerId(testLedger.getId())).thenReturn(List.of(testMember));

        LedgerResponse response = ledgerService.acceptInvitation(testInvitation.getToken(), targetUser);

        assertNotNull(response);
        assertEquals(testLedger.getId(), response.getId());
        verify(ledgerMemberRepository).save(any(LedgerMember.class));
    }

    @Test
    void acceptInvitation_WrongUser_ThrowsAccessDeniedException() {
        User wrongUser = User.builder().id(99L).email("wrong@example.com").build();
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));

        assertThrows(AccessDeniedException.class, () ->
                ledgerService.acceptInvitation(testInvitation.getToken(), wrongUser));
    }

    @Test
    void acceptInvitation_ExpiredToken_ThrowsIllegalArgumentException() {
        testInvitation.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));

        assertThrows(IllegalArgumentException.class, () ->
                ledgerService.acceptInvitation(testInvitation.getToken(), targetUser));
    }

    @Test
    void getLedgerDetails_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.findById(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                ledgerService.getLedgerDetails(testLedger.getId(), testUser));
    }

    @Test
    void getLedgerDetails_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.findById(testLedger.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.getLedgerDetails(testLedger.getId(), testUser));
    }

    @Test
    void inviteUser_AlreadyPending_ThrowsIllegalArgumentException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(1L);
        when(userRepository.findByEmail(targetUser.getEmail())).thenReturn(Optional.of(targetUser));
        when(invitationRepository.existsByLedgerIdAndInvitedEmailAndStatus(
                testLedger.getId(), targetUser.getEmail(), Invitation.InvitationStatus.PENDING)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void acceptInvitation_StatusNotPending_ThrowsIllegalArgumentException() {
        testInvitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));

        assertThrows(IllegalArgumentException.class, () ->
                ledgerService.acceptInvitation(testInvitation.getToken(), targetUser));
    }

    @Test
    void acceptInvitation_LedgerFull_ThrowsLedgerFullException() {
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(2L);

        assertThrows(LedgerFullException.class, () ->
                ledgerService.acceptInvitation(testInvitation.getToken(), targetUser));
    }

    @Test
    void declineInvitation_Success() {
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponse response = ledgerService.declineInvitation(testInvitation.getToken(), targetUser);

        assertNotNull(response);
        assertEquals(Invitation.InvitationStatus.DECLINED.name(), response.getStatus());
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    void declineInvitation_NotPending_ThrowsIllegalArgumentException() {
        testInvitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));

        assertThrows(IllegalArgumentException.class, () ->
                ledgerService.declineInvitation(testInvitation.getToken(), targetUser));
    }

    @Test
    void declineInvitation_WrongUser_ThrowsAccessDeniedException() {
        User wrongUser = User.builder().id(99L).email("wrong@example.com").build();
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));

        assertThrows(AccessDeniedException.class, () ->
                ledgerService.declineInvitation(testInvitation.getToken(), wrongUser));
    }

    @Test
    void getLedgerDetails_Success() {
        LedgerMember secondMember = LedgerMember.builder()
                .id(2L)
                .ledger(testLedger)
                .user(targetUser)
                .joinedAt(LocalDateTime.now())
                .build();

        when(ledgerRepository.findById(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.findByLedgerId(testLedger.getId())).thenReturn(List.of(testMember, secondMember));

        LedgerResponse response = ledgerService.getLedgerDetails(testLedger.getId(), testUser);

        assertNotNull(response);
        assertEquals(testLedger.getId(), response.getId());
        assertEquals(testLedger.getName(), response.getName());
        assertEquals(2, response.getMembers().size());
    }

    @Test
    void inviteUser_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void inviteUser_TargetAlreadyMember_ThrowsIllegalArgumentException() {
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), testUser.getId())).thenReturn(true);
        when(ledgerMemberRepository.countByLedgerId(testLedger.getId())).thenReturn(1L);
        when(userRepository.findByEmail(targetUser.getEmail())).thenReturn(Optional.of(targetUser));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(testLedger.getId(), targetUser.getId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                ledgerService.inviteUser(testLedger.getId(), testUser, targetUser.getEmail()));
    }

    @Test
    void acceptInvitation_InvitationNotFound_ThrowsResourceNotFoundException() {
        when(invitationRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.acceptInvitation("nonexistent", targetUser));
    }

    @Test
    void declineInvitation_InvitationNotFound_ThrowsResourceNotFoundException() {
        when(invitationRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.declineInvitation("nonexistent", targetUser));
    }

    @Test
    void acceptInvitation_LedgerNotFoundDuringLock_ThrowsResourceNotFoundException() {
        when(invitationRepository.findByToken(testInvitation.getToken())).thenReturn(Optional.of(testInvitation));
        when(ledgerRepository.findByIdWithLock(testLedger.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ledgerService.acceptInvitation(testInvitation.getToken(), targetUser));
    }

    @Test
    void getCurrentUserLedger_Success() {
        when(ledgerMemberRepository.findFirstByUserId(testUser.getId())).thenReturn(Optional.of(testMember));
        when(ledgerMemberRepository.findByLedgerId(testLedger.getId())).thenReturn(List.of(testMember));

        Optional<LedgerResponse> response = ledgerService.getCurrentUserLedger(testUser);

        assertTrue(response.isPresent());
        assertEquals(testLedger.getId(), response.get().getId());
    }

    @Test
    void getCurrentUserLedger_UserWithoutLedger_ReturnsEmpty() {
        when(ledgerMemberRepository.findFirstByUserId(testUser.getId())).thenReturn(Optional.empty());

        Optional<LedgerResponse> response = ledgerService.getCurrentUserLedger(testUser);

        assertTrue(response.isEmpty());
    }
}
