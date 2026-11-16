package com.nazeem.invoicemanagement.dto.dashboard;

import java.math.BigDecimal;

public record MonthlySpendingResponse(
        String month,
        BigDecimal totalAmount,
        long invoiceCount
) {}