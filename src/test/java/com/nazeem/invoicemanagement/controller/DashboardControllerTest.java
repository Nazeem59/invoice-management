
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.dashboard.*;
import com.nazeem.invoicemanagement.service.DashboardService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    // ==========================================
    // 1. GET DASHBOARD SUMMARY
    // ==========================================

    @Test
    void getSummaryShouldReturn200() throws Exception {

        when(dashboardService.getSummary()).thenReturn(null);

        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(dashboardService).getSummary();
    }

    // ==========================================
    // 2. GET STATUS SUMMARY
    // ==========================================

    @Test
    void getStatusSummaryShouldReturn200() throws Exception {

        when(dashboardService.getStatusSummary())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/status-summary")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getStatusSummary();
    }

    // ==========================================
    // 3. GET OUTSTANDING INVOICES DEFAULT LIMIT
    // ==========================================

    @Test
    void getOutstandingInvoicesShouldUseDefaultLimit10() throws Exception {

        when(dashboardService.getOutstandingInvoices(10))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/outstanding-invoices")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getOutstandingInvoices(10);
    }

    // ==========================================
    // 4. GET OUTSTANDING INVOICES CUSTOM LIMIT
    // ==========================================

    @Test
    void getOutstandingInvoicesShouldAcceptCustomLimit() throws Exception {

        when(dashboardService.getOutstandingInvoices(5))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/outstanding-invoices")
                        .param("limit", "5")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getOutstandingInvoices(5);
    }

    // ==========================================
    // 5. GET APPROVAL STATISTICS
    // ==========================================

    @Test
    void getApprovalStatisticsShouldReturn200() throws Exception {

        when(dashboardService.getApprovalStatistics())
                .thenReturn(null);

        mockMvc.perform(get("/api/dashboard/approval-statistics")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(dashboardService).getApprovalStatistics();
    }

    // ==========================================
    // 6. GET MONTHLY SPENDING
    // ==========================================

    @Test
    void getMonthlySpendingShouldReturn200() throws Exception {

        when(dashboardService.getMonthlySpending())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/monthly-spending")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getMonthlySpending();
    }

    // ==========================================
    // 7. GET SUPPLIER REPORT DEFAULT LIMIT
    // ==========================================

    @Test
    void getSupplierReportShouldUseDefaultLimit10() throws Exception {

        when(dashboardService.getSupplierReport(10))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/supplier-report")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getSupplierReport(10);
    }

    // ==========================================
    // 8. GET SUPPLIER REPORT CUSTOM LIMIT
    // ==========================================

    @Test
    void getSupplierReportShouldAcceptCustomLimit() throws Exception {

        when(dashboardService.getSupplierReport(5))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/supplier-report")
                        .param("limit", "5")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(dashboardService).getSupplierReport(5);
    }

    // ==========================================
    // 9. UNAUTHENTICATED ACCESS
    // ==========================================

    @Test
    void getSummaryWithoutAuthenticationShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dashboardService);
    }
}
