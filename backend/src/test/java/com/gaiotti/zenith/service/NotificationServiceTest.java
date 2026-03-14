package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.NotificationListResponse;
import com.gaiotti.zenith.model.*;
import com.gaiotti.zenith.repository.InvitationRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User actor;
    private User recipient;
    private Ledger ledger;

    @BeforeEach
    void setUp() {
        actor = User.builder().id(1L).displayName("Gustavo").email("g@example.com").build();
        recipient = User.builder().id(2L).displayName("Parceira").email("p@example.com").build();
        ledger = Ledger.builder().id(10L).name("Casa").build();
    }

    @Test
    void createTransactionNotifications_CreatesOnlyForOtherMembers() {
        Transaction transaction = Transaction.builder()
                .id(99L)
                .ledger(ledger)
                .amount(BigDecimal.valueOf(25))
                .date(LocalDate.of(2026, 3, 4))
                .createdBy(actor)
                .build();

        when(ledgerMemberRepository.findByLedgerId(ledger.getId())).thenReturn(List.of(
                LedgerMember.builder().ledger(ledger).user(actor).build(),
                LedgerMember.builder().ledger(ledger).user(recipient).build()
        ));

        notificationService.createTransactionNotifications(transaction, actor);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<Notification> saved = captor.getValue();

        assertEquals(1, saved.size());
        assertEquals(recipient.getId(), saved.getFirst().getRecipientUser().getId());
        assertEquals(Notification.NotificationType.TRANSACTION_CREATED, saved.getFirst().getType());
    }

    @Test
    void listForUser_ReturnsUnreadCountAndInvitationToken() {
        Notification notification = Notification.builder()
                .id(1L)
                .ledger(ledger)
                .recipientUser(recipient)
                .actorUser(actor)
                .type(Notification.NotificationType.INVITATION_RECEIVED)
                .title("Convite")
                .body("Body")
                .referenceType(Notification.ReferenceType.INVITATION)
                .referenceId(5L)
                .createdAt(LocalDateTime.now())
                .build();

        Invitation invitation = Invitation.builder()
                .id(5L)
                .token("invite-token")
                .build();

        when(notificationRepository.findByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfterOrderByCreatedAtDesc(eq(recipient.getId()), any()))
                .thenReturn(List.of(notification));
        when(notificationRepository.countByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfter(eq(recipient.getId()), any())).thenReturn(1L);
        when(invitationRepository.findAllById(List.of(5L))).thenReturn(List.of(invitation));

        NotificationListResponse response = notificationService.listForUser(recipient, 7, true);

        assertEquals(1L, response.getUnreadCount());
        assertEquals(1, response.getItems().size());
        assertEquals("invite-token", response.getItems().getFirst().getInvitationToken());
    }

    @Test
    void markSeen_WithEmptyIds_DoesNothing() {
        notificationService.markSeen(List.of(), recipient);

        verify(notificationRepository, never()).findByIdInAndRecipientUserId(any(), any());
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void createInvitationNotification_SameActorAndRecipient_DoesNothing() {
        Invitation invitation = Invitation.builder()
                .id(5L)
                .ledger(ledger)
                .invitedBy(actor)
                .build();

        notificationService.createInvitationNotification(invitation, actor);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void listForUser_WhenUnseenOnlyIsFalse_UsesFullQuery() {
        when(notificationRepository.findByRecipientUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(recipient.getId()), any()))
                .thenReturn(List.of());
        when(notificationRepository.countByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfter(eq(recipient.getId()), any())).thenReturn(0L);

        NotificationListResponse response = notificationService.listForUser(recipient, 99, false);

        assertEquals(0L, response.getUnreadCount());
        assertTrue(response.getItems().isEmpty());
        verify(notificationRepository).findByRecipientUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(recipient.getId()), any());
        verify(notificationRepository, never()).findByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfterOrderByCreatedAtDesc(eq(recipient.getId()), any());
    }
}
