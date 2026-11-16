
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.invoice.InvoiceItemRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceItemResponse;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceResponse;
import com.nazeem.invoicemanagement.entity.Invoice;
import com.nazeem.invoicemanagement.entity.InvoiceItem;
import com.nazeem.invoicemanagement.entity.InvoiceStatus;
import com.nazeem.invoicemanagement.entity.Supplier;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.repository.InvoiceRepository;
import com.nazeem.invoicemanagement.repository.SupplierRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public InvoiceResponse createInvoice(
            InvoiceRequest request,
            Long userId) {

        validateDates(request);

        Supplier supplier = findActiveSupplier(request.supplierId());
        User user = findUser(userId);

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setSupplier(supplier);
        invoice.setCreatedBy(user);
        invoice.setInvoiceDate(request.invoiceDate());
        invoice.setDueDate(request.dueDate());
        invoice.setCurrency(normalizeCurrency(request.currency()));
        invoice.setDescription(request.description());
        invoice.setStatus(InvoiceStatus.DRAFT);

        calculateAndAddItems(invoice, request.items());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        InvoiceResponse response = toResponse(savedInvoice);

        auditLogService.logCurrentUser(
                "CREATE",
                "INVOICE",
                savedInvoice.getId(),
                null,
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {

        return invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {

        return toResponse(findInvoice(id));
    }

    public InvoiceResponse updateInvoice(
            Long id,
            InvoiceRequest request
    ) {

        validateDates(request);

        Invoice invoice = findInvoice(id);

        validateDraft(invoice);

        // Capture data before modification
        InvoiceResponse oldValue = toResponse(invoice);

        Supplier supplier = findActiveSupplier(request.supplierId());

        invoice.setSupplier(supplier);
        invoice.setInvoiceDate(request.invoiceDate());
        invoice.setDueDate(request.dueDate());
        invoice.setCurrency(normalizeCurrency(request.currency()));
        invoice.setDescription(request.description());

        // Replace existing items
        invoice.getItems().clear();

        calculateAndAddItems(invoice, request.items());

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        InvoiceResponse newValue = toResponse(updatedInvoice);

        auditLogService.logCurrentUser(
                "UPDATE",
                "INVOICE",
                id,
                oldValue,
                newValue
        );

        return newValue;
    }

    public void deleteInvoice(Long id) {

        Invoice invoice = findInvoice(id);

        validateDraft(invoice);

        InvoiceResponse oldValue = toResponse(invoice);

        invoiceRepository.delete(invoice);

        auditLogService.logCurrentUser(
                "DELETE",
                "INVOICE",
                id,
                oldValue,
                null
        );
    }

    private void calculateAndAddItems(
            Invoice invoice,
            List<InvoiceItemRequest> requests) {

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvoiceItemRequest request : requests) {

            BigDecimal lineSubtotal = request.quantity()
                    .multiply(request.unitPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxAmount = lineSubtotal
                    .multiply(request.taxRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = lineSubtotal.add(taxAmount);

            InvoiceItem item = new InvoiceItem();

            item.setItemDescription(request.itemDescription());
            item.setQuantity(request.quantity());
            item.setUnitPrice(request.unitPrice());
            item.setTaxRate(request.taxRate());
            item.setTaxAmount(taxAmount);
            item.setLineTotal(lineTotal);

            invoice.addItem(item);

            subtotal = subtotal.add(lineSubtotal);
            totalTax = totalTax.add(taxAmount);
        }

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxAmount(totalTax.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotalAmount(
                subtotal.add(totalTax).setScale(2, RoundingMode.HALF_UP)
        );
    }

    private String generateInvoiceNumber() {

        String invoiceNumber;

        do {
            invoiceNumber = "INV-"
                    + LocalDate.now().getYear()
                    + "-"
                    + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);

        } while (invoiceRepository.existsByInvoiceNumber(invoiceNumber));

        return invoiceNumber;
    }

    private String normalizeCurrency(String currency) {

        String result = (currency == null || currency.isBlank())
                ? "IDR"
                : currency.trim().toUpperCase(Locale.ROOT);

        if (!result.matches("[A-Z]{3}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Currency must contain exactly 3 letters"
            );
        }

        return result;
    }

    private void validateDates(InvoiceRequest request) {

        if (request.dueDate().isBefore(request.invoiceDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Due date cannot be before invoice date"
            );
        }
    }

    private void validateDraft(Invoice invoice) {

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only DRAFT invoices can be updated or deleted"
            );
        }
    }

    private Supplier findActiveSupplier(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        if (!Boolean.TRUE.equals(supplier.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier is inactive"
            );
        }

        return supplier;
    }

    private User findUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private Invoice findInvoice(Long id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found"
                ));
    }

    private InvoiceResponse toResponse(Invoice invoice) {

        List<InvoiceItemResponse> items = invoice.getItems()
                .stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId(),
                        item.getItemDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTaxRate(),
                        item.getTaxAmount(),
                        item.getLineTotal()
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getSupplier().getId(),
                invoice.getSupplier().getSupplierName(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getCurrency(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus().name(),
                invoice.getDescription(),
                items
        );
    }
}
