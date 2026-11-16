
package com.nazeem.invoicemanagement.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long supplierId,
        String supplierName,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String status,
        String description,
        List<InvoiceItemResponse> items
) {}
