package com.nazeem.invoicemanagement.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalInvoices,
        BigDecimal totalAmount,
        long draftCount,
        long submittedCount,
        long underReviewCount,
        long approvedCount,
        long rejectedCount,
        long paidCount,
        long cancelledCount
) {}