
package com.nazeem.invoicemanagement.dto.invoice;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long id,
        String itemDescription,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {}
