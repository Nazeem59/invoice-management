
package com.nazeem.invoicemanagement.dto.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvoiceItemRequest(

        @NotBlank(message = "Item description is required")
        @Size(max = 255)
        String itemDescription,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal quantity,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 16, fraction = 2)
        BigDecimal unitPrice,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal taxRate
) {}
