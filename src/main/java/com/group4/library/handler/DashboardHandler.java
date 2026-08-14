package com.group4.library.handler;

import com.group4.library.dto.DashboardResponse;
import com.group4.library.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardHandler {
    private final DashboardService dashboardService;
    public DashboardHandler(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
