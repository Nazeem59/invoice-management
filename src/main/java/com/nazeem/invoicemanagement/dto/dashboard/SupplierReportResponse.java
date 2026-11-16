package com.nazeem.invoicemanagement.dto.dashboard;

import java.math.BigDecimal;

public record SupplierReportResponse(
        Long supplierId,
        String supplierName,
        long invoiceCount,
        BigDecimal totalAmount
) {}