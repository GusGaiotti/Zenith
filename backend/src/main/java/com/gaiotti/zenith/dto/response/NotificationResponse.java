package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private String actorDisplayName;
    private String referenceType;
    private Long referenceId;
    private String invitationToken;
    private LocalDateTime createdAt;
    private LocalDateTime seenAt;
}
