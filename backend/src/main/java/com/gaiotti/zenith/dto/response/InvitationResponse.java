package com.gaiotti.zenith.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private Long id;
    private String token;
    private String invitedEmail;
    private String invitedUserDisplayName;
    private String invitedByDisplayName;
    private String status;
    private LocalDateTime expiresAt;
}
