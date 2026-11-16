package com.nazeem.invoicemanagement.repository;

import com.nazeem.invoicemanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    boolean existsBySupplierCode(String supplierCode);

    boolean existsBySupplierCodeAndIdNot(
            String supplierCode,
            Long id
    );
}