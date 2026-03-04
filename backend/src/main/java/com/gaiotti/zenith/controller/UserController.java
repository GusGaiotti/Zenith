package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.response.MessageResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.AuthCookieService;
import com.gaiotti.zenith.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final AuthUtils authUtils;
    private final AuthCookieService authCookieService;

    @PostMapping("/me/logout")
    public ResponseEntity<MessageResponse> logout() {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        authService.logout(authenticatedUser.getId());

        return ResponseEntity.ok()
                .header("Set-Cookie", authCookieService.buildClearRefreshTokenCookie().toString())
                .body(new MessageResponse("Logged out successfully"));
    }
}
