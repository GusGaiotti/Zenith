package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.UpdateProfileRequest;
import com.gaiotti.zenith.dto.response.MessageResponse;
import com.gaiotti.zenith.dto.response.UserProfileResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthCookieService;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.AuthService;
import com.gaiotti.zenith.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthUtils authUtils;
    private final AuthCookieService authCookieService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        User user = authUtils.getAuthenticatedUser();
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = authUtils.getAuthenticatedUser();
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteAccount() {
        User user = authUtils.getAuthenticatedUser();
        userService.deleteAccount(user);
        return ResponseEntity.ok()
                .header("Set-Cookie", authCookieService.buildClearRefreshTokenCookie().toString())
                .body(new MessageResponse("Account deleted successfully"));
    }

    @PostMapping("/me/logout")
    public ResponseEntity<MessageResponse> logout() {
        User user = authUtils.getAuthenticatedUser();
        authService.logout(user.getId());
        return ResponseEntity.ok()
                .header("Set-Cookie", authCookieService.buildClearRefreshTokenCookie().toString())
                .body(new MessageResponse("Logged out successfully"));
    }
}
