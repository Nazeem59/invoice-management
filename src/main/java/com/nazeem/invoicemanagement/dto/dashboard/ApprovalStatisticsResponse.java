package com.nazeem.invoicemanagement.dto.dashboard;

public record ApprovalStatisticsResponse(
        long pendingCount,
        long approvedCount,
        long rejectedCount
) {}