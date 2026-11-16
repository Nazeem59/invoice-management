
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.AuditLogService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    // ==========================================
    // 1. ADMIN - GET ALL AUDIT LOGS
    // ==========================================

    @Test
    void getAllLogsAsAdminShouldReturn200() throws Exception {

        when(auditLogService.getAllLogs()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/audit-logs")
                        .with(authenticatedUser(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(auditLogService).getAllLogs();
    }

    // ==========================================
    // 2. ADMIN - GET LOGS BY ENTITY
    // ==========================================

    @Test
    void getLogsByEntityAsAdminShouldReturn200() throws Exception {

        when(auditLogService.getLogsByEntity("INVOICE", 100L))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/audit-logs/entity/INVOICE/100")
                        .with(authenticatedUser(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(auditLogService).getLogsByEntity("INVOICE", 100L);
    }

    // ==========================================
    // 3. ADMIN - GET LOGS BY USER
    // ==========================================

    @Test
    void getLogsByUserAsAdminShouldReturn200() throws Exception {

        when(auditLogService.getLogsByUser(2L))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/audit-logs/user/2")
                        .with(authenticatedUser(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(auditLogService).getLogsByUser(2L);
    }

    // ==========================================
    // 4. FINANCE - ACCESS DENIED
    // ==========================================

    @Test
    void getAllLogsAsFinanceShouldReturn403() throws Exception {

        mockMvc.perform(get("/api/audit-logs")
                        .with(authenticatedUser(UserRole.FINANCE)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditLogService);
    }

    // ==========================================
    // 5. APPROVER - ACCESS DENIED
    // ==========================================

    @Test
    void getAllLogsAsApproverShouldReturn403() throws Exception {

        mockMvc.perform(get("/api/audit-logs")
                        .with(authenticatedUser(UserRole.APPROVER)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditLogService);
    }

    // ==========================================
    // 6. UNAUTHENTICATED - ACCESS DENIED
    // ==========================================

    @Test
    void getAllLogsWithoutAuthenticationShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(auditLogService);
    }

    // ==========================================
    // HELPER - CREATE AUTHENTICATED USER
    // ==========================================

    private RequestPostProcessor authenticatedUser(UserRole role) {

        User testUser = new User();

        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(role);
        testUser.setActive(true);

        AppUserDetails principal = new AppUserDetails(testUser);

        return user(principal);
    }
}
