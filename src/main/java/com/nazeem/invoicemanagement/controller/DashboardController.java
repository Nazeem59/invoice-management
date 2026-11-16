
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.dashboard.*;
import com.nazeem.invoicemanagement.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/status-summary")
    public List<InvoiceStatusSummaryResponse> getStatusSummary() {
        return dashboardService.getStatusSummary();
    }

    @GetMapping("/outstanding-invoices")
    public List<OutstandingInvoiceResponse> getOutstandingInvoices(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.getOutstandingInvoices(limit);
    }

    @GetMapping("/approval-statistics")
    public ApprovalStatisticsResponse getApprovalStatistics() {
        return dashboardService.getApprovalStatistics();
    }

    @GetMapping("/monthly-spending")
    public List<MonthlySpendingResponse> getMonthlySpending() {
        return dashboardService.getMonthlySpending();
    }

    @GetMapping("/supplier-report")
    public List<SupplierReportResponse> getSupplierReport(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.getSupplierReport(limit);
    }
}
