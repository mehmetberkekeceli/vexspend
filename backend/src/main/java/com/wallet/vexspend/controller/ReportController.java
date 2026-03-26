package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.report.DashboardReportResponse;
import com.wallet.vexspend.service.DashboardReportService;
import com.wallet.vexspend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Dashboard reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final DashboardReportService dashboardReportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard report")
    public ResponseEntity<DashboardReportResponse> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer trendMonths
    ) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(dashboardReportService.getDashboard(userId, from, to, trendMonths));
    }
}
