package com.gaiotti.zenith.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    private long unreadCount;
    private List<NotificationResponse> items;
}
