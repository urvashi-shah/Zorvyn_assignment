package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.DashboardSummaryResponse;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.summary();
    }

    @GetMapping("/categories")
    public Map<String, BigDecimal> categories() {
        return dashboardService.categoryTotals();
    }

    @GetMapping("/trends")
    public Map<String, BigDecimal> trends() {
        return dashboardService.monthlyTrends();
    }

    @GetMapping("/recent")
    public List<FinancialRecord> recent() {
        return dashboardService.recent();
    }
}
