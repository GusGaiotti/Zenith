package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.NotificationListResponse;
import com.gaiotti.zenith.dto.response.NotificationResponse;
import com.gaiotti.zenith.model.Invitation;
import com.gaiotti.zenith.model.LedgerMember;
import com.gaiotti.zenith.model.Notification;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.InvitationRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final InvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse listForUser(User authenticatedUser, int days, boolean unseenOnly) {
        int normalizedDays = Math.max(1, Math.min(days, 30));
        LocalDateTime createdAfter = LocalDateTime.now().minusDays(normalizedDays);

        List<Notification> notifications = unseenOnly
                ? notificationRepository.findByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfterOrderByCreatedAtDesc(
                authenticatedUser.getId(), createdAfter)
                : notificationRepository.findByRecipientUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                authenticatedUser.getId(), createdAfter);

        Map<Long, Invitation> invitationsById = loadInvitations(notifications);

        return NotificationListResponse.builder()
                .unreadCount(notificationRepository.countByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfter(
                        authenticatedUser.getId(),
                        createdAfter
                ))
                .items(notifications.stream()
                        .map(notification -> mapToResponse(notification, invitationsById))
                        .toList())
                .build();
    }

    @Transactional
    public void markSeen(List<Long> ids, User authenticatedUser) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Notification> notifications = notificationRepository.findByIdInAndRecipientUserId(ids, authenticatedUser.getId());
        LocalDateTime now = LocalDateTime.now();

        notifications.forEach(notification -> {
            if (notification.getSeenAt() == null) {
                notification.setSeenAt(now);
            }
        });

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void createTransactionNotifications(Transaction transaction, User actor) {
        List<LedgerMember> members = ledgerMemberRepository.findByLedgerId(transaction.getLedger().getId());
        List<Notification> notifications = new ArrayList<>();

        for (LedgerMember member : members) {
            User recipient = member.getUser();
            if (recipient.getId().equals(actor.getId())) {
                continue;
            }

            notifications.add(Notification.builder()
                    .ledger(transaction.getLedger())
                    .recipientUser(recipient)
                    .actorUser(actor)
                    .type(Notification.NotificationType.TRANSACTION_CREATED)
                    .title(actor.getDisplayName() + " registrou uma nova transacao")
                    .body(buildTransactionBody(transaction, actor))
                    .referenceType(Notification.ReferenceType.TRANSACTION)
                    .referenceId(transaction.getId())
                    .build());
        }

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Transactional
    public void createInvitationNotification(Invitation invitation, User targetUser) {
        if (targetUser.getId().equals(invitation.getInvitedBy().getId())) {
            return;
        }

        Notification notification = Notification.builder()
                .ledger(invitation.getLedger())
                .recipientUser(targetUser)
                .actorUser(invitation.getInvitedBy())
                .type(Notification.NotificationType.INVITATION_RECEIVED)
                .title("Novo convite para participar da fatura")
                .body(invitation.getInvitedBy().getDisplayName() + " convidou voce para entrar em " + invitation.getLedger().getName())
                .referenceType(Notification.ReferenceType.INVITATION)
                .referenceId(invitation.getId())
                .build();

        notificationRepository.save(notification);
    }

    private String buildTransactionBody(Transaction transaction, User actor) {
        BigDecimal amount = transaction.getAmount();
        String formattedAmount = "R$ " + amount.abs().stripTrailingZeros().toPlainString();
        String formattedCategory = transaction.getCategory() != null ? transaction.getCategory().getName() : "Sem categoria";
        return actor.getDisplayName()
                + " registrou "
                + formattedAmount
                + " em "
                + formattedCategory;
    }

    private Map<Long, Invitation> loadInvitations(List<Notification> notifications) {
        List<Long> invitationIds = notifications.stream()
                .filter(notification -> notification.getReferenceType() == Notification.ReferenceType.INVITATION)
                .map(Notification::getReferenceId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (invitationIds.isEmpty()) {
            return Map.of();
        }

        return invitationRepository.findAllById(invitationIds).stream()
                .collect(Collectors.toMap(Invitation::getId, Function.identity()));
    }

    private NotificationResponse mapToResponse(Notification notification, Map<Long, Invitation> invitationsById) {
        Invitation invitation = notification.getReferenceType() == Notification.ReferenceType.INVITATION
                ? invitationsById.get(notification.getReferenceId())
                : null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .actorDisplayName(notification.getActorUser() != null ? notification.getActorUser().getDisplayName() : null)
                .referenceType(notification.getReferenceType() != null ? notification.getReferenceType().name() : null)
                .referenceId(notification.getReferenceId())
                .invitationToken(invitation != null ? invitation.getToken() : null)
                .createdAt(notification.getCreatedAt())
                .seenAt(notification.getSeenAt())
                .build();
    }
}
