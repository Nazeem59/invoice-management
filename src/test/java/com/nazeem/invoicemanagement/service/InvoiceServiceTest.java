
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.invoice.InvoiceItemRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceResponse;
import com.nazeem.invoicemanagement.entity.Invoice;
import com.nazeem.invoicemanagement.entity.InvoiceStatus;
import com.nazeem.invoicemanagement.entity.Supplier;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.repository.InvoiceRepository;
import com.nazeem.invoicemanagement.repository.SupplierRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Supplier supplier;
    private User user;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierCode("SUP-001");
        supplier.setSupplierName("PT Example Supplier");
        supplier.setActive(true);

        user = new User();
        user.setId(1L);
        user.setFullName("System Administrator");
        user.setEmail("admin@invoice.local");
        user.setActive(true);
    }

    private InvoiceRequest createValidRequest() {
        return new InvoiceRequest(
                1L,
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 12, 15),
                "IDR",
                "IT Infrastructure Equipment",
                List.of(
                        new InvoiceItemRequest(
                                "Laptop Dell Latitude",
                                new BigDecimal("2"),
                                new BigDecimal("12000000"),
                                new BigDecimal("11")
                        ),
                        new InvoiceItemRequest(
                                "Wireless Mouse",
                                new BigDecimal("5"),
                                new BigDecimal("250000"),
                                new BigDecimal("11")
                        )
                )
        );
    }

    @Test
    void createInvoiceShouldCalculateAmountsCorrectly() {
        InvoiceRequest request = createValidRequest();

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(invoiceRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(false);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> {
                    Invoice invoice = invocation.getArgument(0);
                    invoice.setId(100L);
                    return invoice;
                });

        InvoiceResponse response =
                invoiceService.createInvoice(request, 1L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertTrue(response.invoiceNumber().startsWith("INV-"));
        assertEquals("IDR", response.currency());
        assertEquals("DRAFT", response.status());

        assertEquals(
                new BigDecimal("25250000.00"),
                response.subtotal()
        );

        assertEquals(
                new BigDecimal("2777500.00"),
                response.taxAmount()
        );

        assertEquals(
                new BigDecimal("28027500.00"),
                response.totalAmount()
        );

        assertEquals(2, response.items().size());

        verify(invoiceRepository).save(any(Invoice.class));

        verify(auditLogService).logCurrentUser(
                eq("CREATE"),
                eq("INVOICE"),
                eq(100L),
                isNull(),
                any(InvoiceResponse.class)
        );
    }

    @Test
    void createInvoiceShouldRejectInactiveSupplier() {
        supplier.setActive(false);

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invoiceService.createInvoice(
                        createValidRequest(), 1L
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Supplier is inactive",
                exception.getReason()
        );

        verify(invoiceRepository, never()).save(any());
        verify(auditLogService, never()).logCurrentUser(
                anyString(), anyString(), any(), any(), any()
        );
    }

    @Test
    void createInvoiceShouldRejectInvalidDueDate() {
        InvoiceRequest request = new InvoiceRequest(
                1L,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 10),
                "IDR",
                "Invalid date test",
                List.of(
                        new InvoiceItemRequest(
                                "Test Item",
                                BigDecimal.ONE,
                                new BigDecimal("100000"),
                                BigDecimal.ZERO
                        )
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invoiceService.createInvoice(request, 1L)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Due date cannot be before invoice date",
                exception.getReason()
        );

        verify(invoiceRepository, never()).save(any());
    }


    @Test
    void createInvoiceShouldRejectInvalidCurrency() {
        InvoiceRequest request = new InvoiceRequest(
                1L,
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 12, 15),
                "INVALID",
                "Test invoice",
                List.of(
                        new InvoiceItemRequest(
                                "Test Item",
                                BigDecimal.ONE,
                                new BigDecimal("100000"),
                                BigDecimal.ZERO
                        )
                )
        );

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invoiceService.createInvoice(request, 1L)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Currency must contain exactly 3 letters",
                exception.getReason()
        );
    }

    @Test
    void updateInvoiceShouldRejectNonDraftInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(100L);
        invoice.setStatus(InvoiceStatus.APPROVED);

        when(invoiceRepository.findById(100L))
                .thenReturn(Optional.of(invoice));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invoiceService.updateInvoice(
                        100L, createValidRequest()
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "Only DRAFT invoices can be updated or deleted",
                exception.getReason()
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void deleteInvoiceShouldRejectNonDraftInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(100L);
        invoice.setStatus(InvoiceStatus.APPROVED);

        when(invoiceRepository.findById(100L))
                .thenReturn(Optional.of(invoice));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invoiceService.deleteInvoice(100L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "Only DRAFT invoices can be updated or deleted",
                exception.getReason()
        );

        verify(invoiceRepository, never()).delete(any());
        verify(auditLogService, never()).logCurrentUser(
                anyString(), anyString(), any(), any(), any()
        );
    }
}
