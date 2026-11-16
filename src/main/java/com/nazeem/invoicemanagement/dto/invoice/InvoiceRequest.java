
package com.nazeem.invoicemanagement.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record InvoiceRequest(

        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotNull(message = "Invoice date is required")
        LocalDate invoiceDate,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @Size(min = 3, max = 3)
        String currency,

        String description,

        @NotEmpty(message = "At least one invoice item is required")
        List<@Valid InvoiceItemRequest> items
) {}
