
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.invoice.InvoiceRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceResponse;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.InvoiceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(
            @Valid @RequestBody InvoiceRequest request,
            @AuthenticationPrincipal AppUserDetails user) {

        return invoiceService.createInvoice(
                request,
                user.getId()
        );
    }

    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PutMapping("/{id}")
    public InvoiceResponse updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {

        return invoiceService.updateInvoice(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }
}
