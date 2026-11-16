
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.approval.ApprovalRequest;
import com.nazeem.invoicemanagement.dto.approval.ApprovalResponse;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.ApprovalService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprovalService approvalService;

    private RequestPostProcessor authenticatedUser(
            Long userId,
            String fullName,
            String email,
            UserRole role
    ) {
        User testUser = new User();

        testUser.setId(userId);
        testUser.setFullName(fullName);
        testUser.setEmail(email);
        testUser.setRole(role);
        testUser.setActive(true);

        AppUserDetails principal = new AppUserDetails(testUser);

        return user(principal);
    }

    private RequestPostProcessor financeUser() {
        return authenticatedUser(
                1L,
                "Finance User",
                "finance@invoice.local",
                UserRole.FINANCE
        );
    }

    private RequestPostProcessor adminUser() {
        return authenticatedUser(
                2L,
                "System Administrator",
                "admin@invoice.local",
                UserRole.ADMIN
        );
    }

    private RequestPostProcessor approverUser() {
        return authenticatedUser(
                3L,
                "Invoice Approver",
                "approver@invoice.local",
                UserRole.APPROVER
        );
    }

    private ApprovalResponse createApprovalResponse(String decision) {
        return new ApprovalResponse(
                1L,
                100L,
                3L,
                "Invoice Approver",
                1,
                decision,
                "Reviewed successfully",
                LocalDateTime.of(2026, 11, 16, 10, 30)
        );
    }

    @Test
    void submitInvoiceShouldReturnSuccessForFinance() throws Exception {

        mockMvc.perform(post("/api/invoices/{id}/submit", 100L)
                        .with(financeUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Invoice submitted successfully"))
                .andExpect(jsonPath("$.status")
                        .value("UNDER_REVIEW"));

        verify(approvalService).submitInvoice(100L);
    }

    @Test
    void submitInvoiceShouldReturnSuccessForAdmin() throws Exception {

        mockMvc.perform(post("/api/invoices/{id}/submit", 100L)
                        .with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Invoice submitted successfully"))
                .andExpect(jsonPath("$.status")
                        .value("UNDER_REVIEW"));

        verify(approvalService).submitInvoice(100L);
    }

    @Test
    void approveInvoiceShouldReturnApprovalResponse() throws Exception {

        when(approvalService.approveInvoice(
                eq(100L),
                eq(3L),
                any()
        )).thenReturn(createApprovalResponse("APPROVED"));

        mockMvc.perform(post("/api/invoices/{id}/approve", 100L)
                        .with(approverUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceId").value(100))
                .andExpect(jsonPath("$.approverId").value(3))
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.comments")
                        .value("Reviewed successfully"));

        verify(approvalService).approveInvoice(
                eq(100L),
                eq(3L),
                any()
        );
    }

    @Test
    void rejectInvoiceShouldReturnApprovalResponse() throws Exception {

        when(approvalService.rejectInvoice(
                eq(100L),
                eq(3L),
                any()
        )).thenReturn(createApprovalResponse("REJECTED"));

        mockMvc.perform(post("/api/invoices/{id}/reject", 100L)
                        .with(approverUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(100))
                .andExpect(jsonPath("$.decision").value("REJECTED"))
                .andExpect(jsonPath("$.approverName")
                        .value("Invoice Approver"));

        verify(approvalService).rejectInvoice(
                eq(100L),
                eq(3L),
                any()
        );
    }

    @Test
    void getInvoiceApprovalsShouldReturnList() throws Exception {

        when(approvalService.getInvoiceApprovals(100L))
                .thenReturn(List.of(createApprovalResponse("PENDING")));

        mockMvc.perform(get("/api/invoices/{id}/approvals", 100L)
                        .with(financeUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].invoiceId").value(100))
                .andExpect(jsonPath("$[0].decision").value("PENDING"));

        verify(approvalService).getInvoiceApprovals(100L);
    }

    @Test
    void approveInvoiceShouldReturnForbiddenForFinanceUser()
            throws Exception {

        mockMvc.perform(post("/api/invoices/{id}/approve", 100L)
                        .with(financeUser()))
                .andExpect(status().isForbidden());

    }

    @Test
    void submitInvoiceShouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(post("/api/invoices/{id}/submit", 100L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void approveInvoiceShouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {

        mockMvc.perform(post("/api/invoices/{id}/approve", 100L))
                .andExpect(status().isUnauthorized());
    }
}
