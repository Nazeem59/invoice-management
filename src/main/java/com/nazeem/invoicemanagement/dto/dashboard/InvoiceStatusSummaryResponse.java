package com.nazeem.invoicemanagement.dto.dashboard;

public record InvoiceStatusSummaryResponse(
        String status,
        long count
) {}