package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.dto.response.AskAiResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.ai.AskAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/ai")
@RequiredArgsConstructor
public class AskAiController {

    private final AskAiService askAiService;
    private final AuthUtils authUtils;

    @PostMapping("/ask")
    public ResponseEntity<AskAiResponse> ask(
            @PathVariable Long ledgerId,
            @Valid @RequestBody AskAiRequest request
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        AskAiResponse response = askAiService.ask(ledgerId, authenticatedUser, request);
        return ResponseEntity.ok(response);
    }
}
