package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.CreateLedgerRequest;
import com.gaiotti.zenith.dto.request.InviteUserRequest;
import com.gaiotti.zenith.dto.request.UpdateLedgerRequest;
import com.gaiotti.zenith.dto.response.InvitationResponse;
import com.gaiotti.zenith.dto.response.LedgerResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<LedgerResponse> createLedger(@Valid @RequestBody CreateLedgerRequest request) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        LedgerResponse response = ledgerService.createLedger(request.getName(), authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LedgerResponse> getLedgerDetails(@PathVariable Long id) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        LedgerResponse response = ledgerService.getLedgerDetails(id, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LedgerResponse> updateLedger(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLedgerRequest request) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        LedgerResponse response = ledgerService.updateLedgerName(id, request.getName(), authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LedgerResponse> getMyLedger() {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        Optional<LedgerResponse> response = ledgerService.getCurrentUserLedger(authenticatedUser);
        return ResponseEntity.of(response);
    }

    @PostMapping("/{id}/invitations")
    public ResponseEntity<InvitationResponse> inviteUser(
            @PathVariable Long id,
            @Valid @RequestBody InviteUserRequest request) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        InvitationResponse response = ledgerService.inviteUser(id, authenticatedUser, request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/invitations/{token}/accept")
    public ResponseEntity<LedgerResponse> acceptInvitation(@PathVariable String token) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        LedgerResponse response = ledgerService.acceptInvitation(token, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/invitations/{token}/decline")
    public ResponseEntity<InvitationResponse> declineInvitation(@PathVariable String token) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        InvitationResponse response = ledgerService.declineInvitation(token, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/invitations/{token}/cancel")
    public ResponseEntity<InvitationResponse> cancelInvitation(@PathVariable String token) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        InvitationResponse response = ledgerService.cancelInvitation(token, authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
