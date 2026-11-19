
package com.nazeem.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazeem.invoicemanagement.dto.supplier.SupplierRequest;
import com.nazeem.invoicemanagement.dto.supplier.SupplierResponse;
import com.nazeem.invoicemanagement.service.SupplierService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    private SupplierResponse createSupplierResponse() {
        return new SupplierResponse(
                1L,
                "SUP-001",
                "PT Teknologi Indonesia",
                "vendor@example.com",
                "+628123456789",
                "NPWP-123456",
                "Jl. Sudirman No. 10",
                "Jakarta",
                "Indonesia",
                "BCA",
                "1234567890",
                true
        );
    }

    private SupplierRequest createSupplierRequest() {
        return new SupplierRequest(
                "SUP-001",
                "PT Teknologi Indonesia",
                "vendor@example.com",
                "+628123456789",
                "NPWP-123456",
                "Jl. Sudirman No. 10",
                "Jakarta",
                "Indonesia",
                "BCA",
                "1234567890",
                true
        );
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getAllSuppliersShouldReturnList() throws Exception {

        when(supplierService.getAllSuppliers())
                .thenReturn(List.of(createSupplierResponse()));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].supplierCode")
                        .value("SUP-001"))
                .andExpect(jsonPath("$[0].supplierName")
                        .value("PT Teknologi Indonesia"));

        verify(supplierService).getAllSuppliers();
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getSupplierByIdShouldReturnSupplier() throws Exception {

        when(supplierService.getSupplierById(1L))
                .thenReturn(createSupplierResponse());

        mockMvc.perform(get("/api/suppliers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.supplierCode")
                        .value("SUP-001"))
                .andExpect(jsonPath("$.email")
                        .value("vendor@example.com"));

        verify(supplierService).getSupplierById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSupplierShouldReturnCreated() throws Exception {

        SupplierRequest request = createSupplierRequest();

        when(supplierService.createSupplier(any(SupplierRequest.class)))
                .thenReturn(createSupplierResponse());

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.supplierCode")
                        .value("SUP-001"))
                .andExpect(jsonPath("$.supplierName")
                        .value("PT Teknologi Indonesia"));

        verify(supplierService).createSupplier(any(SupplierRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSupplierShouldReturnBadRequestWhenEmailIsInvalid()
            throws Exception {

        String request = """
                {
                    "supplierCode": "SUP-002",
                    "supplierName": "PT Example Indonesia",
                    "email": "invalid-email",
                    "phone": "+628123456789",
                    "taxNumber": "NPWP-123456",
                    "address": "Jakarta",
                    "city": "Jakarta",
                    "country": "Indonesia",
                    "bankName": "BCA",
                    "bankAccountNumber": "1234567890",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(supplierService, never())
                .createSupplier(any(SupplierRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSupplierShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/suppliers/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(supplierService).deleteSupplier(1L);
    }

    @Test
    void getAllSuppliersShouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isUnauthorized());

        verify(supplierService, never()).getAllSuppliers();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSupplierShouldReturnUpdatedSupplier() throws Exception {

        SupplierRequest request = createSupplierRequest();

        when(supplierService.updateSupplier(
                eq(1L),
                any(SupplierRequest.class)
        )).thenReturn(createSupplierResponse());

        mockMvc.perform(put("/api/suppliers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.supplierCode").value("SUP-001"))
                .andExpect(jsonPath("$.supplierName")
                        .value("PT Teknologi Indonesia"));

        verify(supplierService).updateSupplier(
                eq(1L),
                any(SupplierRequest.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSupplierShouldReturnConflictWhenSupplierCodeAlreadyExists()
            throws Exception {

        SupplierRequest request = createSupplierRequest();

        when(supplierService.createSupplier(any(SupplierRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Supplier code already exists"
                ));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(supplierService).createSupplier(any(SupplierRequest.class));
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getSupplierByIdShouldReturnNotFoundWhenSupplierDoesNotExist()
            throws Exception {

        when(supplierService.getSupplierById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        mockMvc.perform(get("/api/suppliers/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(supplierService).getSupplierById(999L);
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void createSupplierShouldReturnForbiddenForApprover() throws Exception {

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createSupplierRequest())))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).createSupplier(any());
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void updateSupplierShouldReturnForbiddenForApprover() throws Exception {

        mockMvc.perform(put("/api/suppliers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createSupplierRequest())))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).updateSupplier(any(), any());
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void deleteSupplierShouldReturnForbiddenForApprover() throws Exception {

        mockMvc.perform(delete("/api/suppliers/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).deleteSupplier(any());
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void getAllSuppliersShouldBeAllowedForApprover() throws Exception {

        when(supplierService.getAllSuppliers()).thenReturn(List.of());

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk());
    }

}
