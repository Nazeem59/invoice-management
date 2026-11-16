package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.supplier.SupplierRequest;
import com.nazeem.invoicemanagement.dto.supplier.SupplierResponse;
import com.nazeem.invoicemanagement.entity.Supplier;
import com.nazeem.invoicemanagement.repository.SupplierRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    public SupplierService(
            SupplierRepository supplierRepository,
            AuditLogService auditLogService
    ) {
        this.supplierRepository = supplierRepository;
        this.auditLogService = auditLogService;
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = findSupplier(id);
        return toResponse(supplier);
    }

    public SupplierResponse createSupplier(SupplierRequest request) {

        if (supplierRepository.existsBySupplierCode(
                request.supplierCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Supplier code already exists"
            );
        }

        Supplier supplier = new Supplier();

        mapRequestToEntity(request, supplier);

        Supplier savedSupplier = supplierRepository.save(supplier);

        SupplierResponse response = toResponse(savedSupplier);

        auditLogService.logCurrentUser(
                "CREATE",
                "SUPPLIER",
                savedSupplier.getId(),
                null,
                response
        );

        return response;
    }

    public SupplierResponse updateSupplier(
            Long id,
            SupplierRequest request
    ) {

        Supplier supplier = findSupplier(id);

        SupplierResponse oldValue = toResponse(supplier);

        if (supplierRepository.existsBySupplierCodeAndIdNot(
                request.supplierCode(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Supplier code already exists"
            );
        }

        mapRequestToEntity(request, supplier);

        Supplier updatedSupplier = supplierRepository.save(supplier);

        SupplierResponse newValue = toResponse(updatedSupplier);

        auditLogService.logCurrentUser(
                "UPDATE",
                "SUPPLIER",
                id,
                oldValue,
                newValue
        );

        return newValue;
    }

    public void deleteSupplier(Long id) {

        Supplier supplier = findSupplier(id);

        SupplierResponse oldValue = toResponse(supplier);

        supplierRepository.delete(supplier);

        auditLogService.logCurrentUser(
                "DELETE",
                "SUPPLIER",
                id,
                oldValue,
                null
        );
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));
    }

    private void mapRequestToEntity(
            SupplierRequest request,
            Supplier supplier
    ) {
        supplier.setSupplierCode(request.supplierCode());
        supplier.setSupplierName(request.supplierName());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setTaxNumber(request.taxNumber());
        supplier.setAddress(request.address());
        supplier.setCity(request.city());
        supplier.setCountry(request.country());
        supplier.setBankName(request.bankName());
        supplier.setBankAccountNumber(request.bankAccountNumber());

        if (request.active() != null) {
            supplier.setActive(request.active());
        } else if (supplier.getId() == null) {
            supplier.setActive(true);
        }
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getTaxNumber(),
                supplier.getAddress(),
                supplier.getCity(),
                supplier.getCountry(),
                supplier.getBankName(),
                supplier.getBankAccountNumber(),
                supplier.getActive()
        );
    }
}