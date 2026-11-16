package com.nazeem.invoicemanagement.dto.supplier;

public record SupplierResponse(
        Long id,
        String supplierCode,
        String supplierName,
        String email,
        String phone,
        String taxNumber,
        String address,
        String city,
        String country,
        String bankName,
        String bankAccountNumber,
        Boolean active
) {}