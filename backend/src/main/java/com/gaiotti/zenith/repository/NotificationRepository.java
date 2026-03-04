package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByRecipientUserIdAndSeenAtIsNull(Long recipientUserId);
    List<Notification> findByRecipientUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long recipientUserId, LocalDateTime createdAfter);
    List<Notification> findByRecipientUserIdAndSeenAtIsNullAndCreatedAtAfterOrderByCreatedAtDesc(Long recipientUserId, LocalDateTime createdAfter);
    List<Notification> findByIdInAndRecipientUserId(List<Long> ids, Long recipientUserId);
}
