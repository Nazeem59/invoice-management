
package com.nazeem.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceItemRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceRequest;
import com.nazeem.invoicemanagement.dto.invoice.InvoiceResponse;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.InvoiceService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

    private InvoiceResponse createInvoiceResponse() {

        return new InvoiceResponse(
                1L,
                "INV-2026-ABC12345",
                1L,
                "PT Teknologi Indonesia",
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 12, 15),
                "IDR",
                new BigDecimal("25250000.00"),
                new BigDecimal("2777500.00"),
                new BigDecimal("28027500.00"),
                "DRAFT",
                "IT Infrastructure Equipment",
                List.of()
        );
    }

    private InvoiceRequest createInvoiceRequest() {

        InvoiceItemRequest item = new InvoiceItemRequest(
                "Laptop Dell Latitude",
                new BigDecimal("2"),
                new BigDecimal("12000000"),
                new BigDecimal("11")
        );

        return new InvoiceRequest(
                1L,
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 12, 15),
                "IDR",
                "IT Infrastructure Equipment",
                List.of(item)
        );
    }

    private RequestPostProcessor financeUser() {

        User testUser = new User();

        testUser.setId(1L);
        testUser.setFullName("Finance User");
        testUser.setEmail("finance@invoice.local");
        testUser.setRole(UserRole.FINANCE);
        testUser.setActive(true);

        AppUserDetails principal = new AppUserDetails(testUser);

        return user(principal);
    }

    @Test
    void createInvoiceShouldReturnCreated() throws Exception {

        InvoiceRequest request = createInvoiceRequest();

        when(invoiceService.createInvoice(
                any(InvoiceRequest.class),
                eq(1L)
        )).thenReturn(createInvoiceResponse());

        mockMvc.perform(post("/api/invoices")
                        .with(financeUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber")
                        .value("INV-2026-ABC12345"))
                .andExpect(jsonPath("$.supplierId").value(1))
                .andExpect(jsonPath("$.currency").value("IDR"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(28027500.00));

        verify(invoiceService).createInvoice(
                any(InvoiceRequest.class),
                eq(1L)
        );
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            roles = "FINANCE"
    )
    void getAllInvoicesShouldReturnList() throws Exception {

        when(invoiceService.getAllInvoices())
                .thenReturn(List.of(createInvoiceResponse()));

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].invoiceNumber")
                        .value("INV-2026-ABC12345"))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        verify(invoiceService).getAllInvoices();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            roles = "FINANCE"
    )
    void getInvoiceByIdShouldReturnInvoice() throws Exception {

        when(invoiceService.getInvoiceById(1L))
                .thenReturn(createInvoiceResponse());

        mockMvc.perform(get("/api/invoices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber")
                        .value("INV-2026-ABC12345"))
                .andExpect(jsonPath("$.supplierName")
                        .value("PT Teknologi Indonesia"));

        verify(invoiceService).getInvoiceById(1L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            roles = "FINANCE"
    )
    void updateInvoiceShouldReturnUpdatedInvoice() throws Exception {

        InvoiceRequest request = createInvoiceRequest();

        when(invoiceService.updateInvoice(
                eq(1L),
                any(InvoiceRequest.class)
        )).thenReturn(createInvoiceResponse());

        mockMvc.perform(put("/api/invoices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber")
                        .value("INV-2026-ABC12345"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(28027500.00));

        verify(invoiceService).updateInvoice(
                eq(1L),
                any(InvoiceRequest.class)
        );
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            roles = "ADMIN"
    )
    void deleteInvoiceShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/invoices/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(invoiceService).deleteInvoice(1L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            roles = "FINANCE"
    )
    void createInvoiceShouldReturnBadRequestWhenSupplierIdIsMissing()
            throws Exception {

        String request = """
                {
                    "supplierId": null,
                    "invoiceDate": "2026-11-15",
                    "dueDate": "2026-12-15",
                    "currency": "IDR",
                    "description": "IT Infrastructure Equipment",
                    "items": []
                }
                """;

        mockMvc.perform(post("/api/invoices")
                        .with(financeUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(invoiceService, never()).createInvoice(
                any(InvoiceRequest.class),
                any(Long.class)
        );
    }

    @Test
    void getAllInvoicesShouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isUnauthorized());

        verify(invoiceService, never()).getAllInvoices();
    }
}
