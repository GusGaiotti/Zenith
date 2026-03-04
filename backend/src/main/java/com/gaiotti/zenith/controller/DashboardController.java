package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.response.*;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardResponse response = dashboardService.getDashboard(ledgerId, authenticatedUser, yearMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardOverviewResponse response = dashboardService.getOverview(ledgerId, authenticatedUser, yearMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/couple-split")
    public ResponseEntity<DashboardCoupleSplitResponse> getCoupleSplit(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardCoupleSplitResponse response = dashboardService.getCoupleSplit(ledgerId, authenticatedUser, yearMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trends")
    public ResponseEntity<DashboardTrendsResponse> getTrends(
            @PathVariable Long ledgerId,
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth endMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardTrendsResponse response = dashboardService.getTrends(ledgerId, authenticatedUser, months, endMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/breakdown")
    public ResponseEntity<DashboardCategoriesBreakdownResponse> getCategoriesBreakdown(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardCategoriesBreakdownResponse response = dashboardService.getCategoriesBreakdown(ledgerId, authenticatedUser, yearMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pulse")
    public ResponseEntity<DashboardPulseResponse> getPulse(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam(required = false) Long createdByUserId
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        DashboardPulseResponse response = dashboardService.getPulse(ledgerId, authenticatedUser, yearMonth, createdByUserId);
        return ResponseEntity.ok(response);
    }
}
