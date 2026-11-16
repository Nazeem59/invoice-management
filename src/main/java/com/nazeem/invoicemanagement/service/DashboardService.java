
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.dashboard.*;
import com.nazeem.invoicemanagement.repository.DashboardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardSummaryResponse getSummary() {
        Object[] row = dashboardRepository.getDashboardSummary().get(0);

        return new DashboardSummaryResponse(
                toLong(row[0]),
                toBigDecimal(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5]),
                toLong(row[6]),
                toLong(row[7]),
                toLong(row[8])
        );
    }

    public List<OutstandingInvoiceResponse> getOutstandingInvoices(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));

        List<Object[]> rows = dashboardRepository.getOutstandingInvoices(
                PageRequest.of(0, safeLimit)
        );

        List<OutstandingInvoiceResponse> responses = new ArrayList<>();

        for (Object[] row : rows) {
            responses.add(new OutstandingInvoiceResponse(
                    toLong(row[0]),
                    (String) row[1],
                    (String) row[2],
                    toLocalDate(row[3]),
                    toLocalDate(row[4]),
                    (String) row[5],
                    toBigDecimal(row[6]),
                    (String) row[7]
            ));
        }

        return responses;
    }

    public ApprovalStatisticsResponse getApprovalStatistics() {
        Object[] row = dashboardRepository.getApprovalStatistics().get(0);

        return new ApprovalStatisticsResponse(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2])
        );
    }

    public List<MonthlySpendingResponse> getMonthlySpending() {
        List<Object[]> rows = dashboardRepository.getMonthlySpending();

        List<MonthlySpendingResponse> responses = new ArrayList<>();

        for (Object[] row : rows) {
            responses.add(new MonthlySpendingResponse(
                    (String) row[0],
                    toBigDecimal(row[1]),
                    toLong(row[2])
            ));
        }

        return responses;
    }

    public List<SupplierReportResponse> getSupplierReport(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));

        List<Object[]> rows = dashboardRepository.getSupplierReport(
                PageRequest.of(0, safeLimit)
        );

        List<SupplierReportResponse> responses = new ArrayList<>();

        for (Object[] row : rows) {
            responses.add(new SupplierReportResponse(
                    toLong(row[0]),
                    (String) row[1],
                    toLong(row[2]),
                    toBigDecimal(row[3])
            ));
        }

        return responses;
    }

    public List<InvoiceStatusSummaryResponse> getStatusSummary() {
        DashboardSummaryResponse summary = getSummary();

        return List.of(
                new InvoiceStatusSummaryResponse("DRAFT", summary.draftCount()),
                new InvoiceStatusSummaryResponse("SUBMITTED", summary.submittedCount()),
                new InvoiceStatusSummaryResponse("UNDER_REVIEW", summary.underReviewCount()),
                new InvoiceStatusSummaryResponse("APPROVED", summary.approvedCount()),
                new InvoiceStatusSummaryResponse("REJECTED", summary.rejectedCount()),
                new InvoiceStatusSummaryResponse("PAID", summary.paidCount()),
                new InvoiceStatusSummaryResponse("CANCELLED", summary.cancelledCount())
        );
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date date) {
            return date.toLocalDate();
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        return LocalDate.parse(value.toString());
    }
}
