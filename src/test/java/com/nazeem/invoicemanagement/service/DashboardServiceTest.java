
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.dashboard.*;
import com.nazeem.invoicemanagement.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummaryShouldReturnCorrectSummary() {
        Object[] row = {
                10L,
                new BigDecimal("50000000"),
                2L, 1L, 2L, 2L, 1L, 1L, 1L
        };

        when(dashboardRepository.getDashboardSummary())
                .thenReturn(List.<Object[]>of(row));

        DashboardSummaryResponse response =
                dashboardService.getSummary();

        assertEquals(10L, response.totalInvoices());
        assertEquals(
                new BigDecimal("50000000"),
                response.totalAmount()
        );
        assertEquals(2L, response.draftCount());
        assertEquals(1L, response.submittedCount());
        assertEquals(2L, response.underReviewCount());
        assertEquals(2L, response.approvedCount());
        assertEquals(1L, response.rejectedCount());
        assertEquals(1L, response.paidCount());
        assertEquals(1L, response.cancelledCount());

        verify(dashboardRepository).getDashboardSummary();
    }

    @Test
    void getOutstandingInvoicesShouldReturnInvoiceList() {
        Object[] row = {
                1L,
                "INV-2026-001",
                "PT Example Supplier",
                Date.valueOf(LocalDate.of(2026, 11, 1)),
                Date.valueOf(LocalDate.of(2026, 11, 30)),
                "IDR",
                new BigDecimal("15000000"),
                "APPROVED"
        };

        when(dashboardRepository.getOutstandingInvoices(
                PageRequest.of(0, 5)
        )).thenReturn(List.<Object[]>of(row));

        List<OutstandingInvoiceResponse> responses =
                dashboardService.getOutstandingInvoices(5);

        assertEquals(1, responses.size());

        OutstandingInvoiceResponse response = responses.get(0);

        assertEquals(1L, response.invoiceId());
        assertEquals("INV-2026-001", response.invoiceNumber());
        assertEquals("PT Example Supplier", response.supplierName());
        assertEquals("IDR", response.currency());
        assertEquals(
                new BigDecimal("15000000"),
                response.totalAmount()
        );
        assertEquals("APPROVED", response.status());

        verify(dashboardRepository).getOutstandingInvoices(
                PageRequest.of(0, 5)
        );
    }

    @Test
    void getOutstandingInvoicesShouldLimitMaximumTo100() {
        when(dashboardRepository.getOutstandingInvoices(
                PageRequest.of(0, 100)
        )).thenReturn(List.of());

        dashboardService.getOutstandingInvoices(500);

        verify(dashboardRepository).getOutstandingInvoices(
                PageRequest.of(0, 100)
        );
    }

    @Test
    void getApprovalStatisticsShouldReturnCorrectCounts() {
        Object[] row = {3L, 5L, 2L};

        when(dashboardRepository.getApprovalStatistics())
                .thenReturn(List.<Object[]>of(row));

        ApprovalStatisticsResponse response =
                dashboardService.getApprovalStatistics();

        assertEquals(3L, response.pendingCount());
        assertEquals(5L, response.approvedCount());
        assertEquals(2L, response.rejectedCount());
    }

    @Test
    void getMonthlySpendingShouldReturnMonthlyData() {
        Object[] row = {
                "2026-11",
                new BigDecimal("25000000"),
                4L
        };

        when(dashboardRepository.getMonthlySpending())
                .thenReturn(List.<Object[]>of(row));

        List<MonthlySpendingResponse> responses =
                dashboardService.getMonthlySpending();

        assertEquals(1, responses.size());
        assertEquals("2026-11", responses.get(0).month());
        assertEquals(
                new BigDecimal("25000000"),
                responses.get(0).totalAmount()
        );
        assertEquals(4L, responses.get(0).invoiceCount());
    }

    @Test
    void getSupplierReportShouldReturnSupplierData() {
        Object[] row = {
                1L,
                "PT Example Supplier",
                5L,
                new BigDecimal("75000000")
        };

        when(dashboardRepository.getSupplierReport(
                PageRequest.of(0, 10)
        )).thenReturn(List.<Object[]>of(row));

        List<SupplierReportResponse> responses =
                dashboardService.getSupplierReport(10);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).supplierId());
        assertEquals(
                "PT Example Supplier",
                responses.get(0).supplierName()
        );
        assertEquals(5L, responses.get(0).invoiceCount());
        assertEquals(
                new BigDecimal("75000000"),
                responses.get(0).totalAmount()
        );
    }

    @Test
    void getStatusSummaryShouldReturnAllInvoiceStatuses() {
        Object[] row = {
                10L,
                new BigDecimal("50000000"),
                2L, 1L, 2L, 2L, 1L, 1L, 1L
        };

        when(dashboardRepository.getDashboardSummary())
                .thenReturn(List.<Object[]>of(row));

        List<InvoiceStatusSummaryResponse> responses =
                dashboardService.getStatusSummary();

        assertEquals(7, responses.size());
        assertEquals("DRAFT", responses.get(0).status());
        assertEquals(2L, responses.get(0).count());
        assertEquals("CANCELLED", responses.get(6).status());
        assertEquals(1L, responses.get(6).count());
    }
}
