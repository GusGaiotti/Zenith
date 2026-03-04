package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.MarkNotificationsSeenRequest;
import com.gaiotti.zenith.dto.response.MessageResponse;
import com.gaiotti.zenith.dto.response.NotificationListResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<NotificationListResponse> listNotifications(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "true") boolean unseenOnly) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        NotificationListResponse response = notificationService.listForUser(authenticatedUser, days, unseenOnly);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/mark-seen")
    public ResponseEntity<MessageResponse> markSeen(@Valid @RequestBody MarkNotificationsSeenRequest request) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        notificationService.markSeen(request.getIds(), authenticatedUser);
        return ResponseEntity.ok(new MessageResponse("Notifications marked as seen"));
    }
}
