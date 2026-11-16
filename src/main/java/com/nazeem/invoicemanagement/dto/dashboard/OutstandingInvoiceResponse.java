package com.nazeem.invoicemanagement.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OutstandingInvoiceResponse(
        Long invoiceId,
        String invoiceNumber,
        String supplierName,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String currency,
        BigDecimal totalAmount,
        String status
) {}