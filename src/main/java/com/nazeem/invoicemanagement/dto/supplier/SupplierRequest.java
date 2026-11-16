package com.nazeem.invoicemanagement.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(

        @NotBlank(message = "Supplier code is required")
        @Size(max = 50)
        String supplierCode,

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150)
        String supplierName,

        @Email(message = "Invalid email format")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 50)
        String taxNumber,

        String address,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String country,

        @Size(max = 100)
        String bankName,

        @Size(max = 100)
        String bankAccountNumber,

        Boolean active
) {}