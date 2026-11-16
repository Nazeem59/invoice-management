
package com.nazeem.invoicemanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazeem.invoicemanagement.dto.audit.AuditLogResponse;
import com.nazeem.invoicemanagement.entity.AuditLog;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.repository.AuditLogRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;
import com.nazeem.invoicemanagement.security.AppUserDetails;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    private User user;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        user = new User();
        user.setId(1L);
        user.setFullName("System Administrator");
        user.setEmail("admin@invoice.local");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        auditLogService = new AuditLogService(
                auditLogRepository,
                userRepository,
                objectMapper
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void logShouldSaveAuditLogWithJsonValues() throws Exception {

        AuditLogService service = auditLogService;

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Map<String, String> oldValue = Map.of(
                "status", "DRAFT"
        );

        Map<String, String> newValue = Map.of(
                "status", "UNDER_REVIEW"
        );

        service.log(
                1L,
                "SUBMIT",
                "INVOICE",
                100L,
                oldValue,
                newValue,
                "127.0.0.1"
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertEquals(user, savedLog.getUser());
        assertEquals("SUBMIT", savedLog.getAction());
        assertEquals("INVOICE", savedLog.getEntityType());
        assertEquals(100L, savedLog.getEntityId());
        assertEquals("{\"status\":\"DRAFT\"}", savedLog.getOldValue());
        assertEquals(
                "{\"status\":\"UNDER_REVIEW\"}",
                savedLog.getNewValue()
        );
        assertEquals("127.0.0.1", savedLog.getIpAddress());
    }

    @Test
    void logShouldSaveWithoutUser() {

        auditLogService.log(
                null,
                "SYSTEM_ACTION",
                "INVOICE",
                100L,
                null,
                null,
                null
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertNull(savedLog.getUser());
        assertEquals("SYSTEM_ACTION", savedLog.getAction());
        assertEquals("INVOICE", savedLog.getEntityType());
        assertEquals(100L, savedLog.getEntityId());
        assertNull(savedLog.getOldValue());
        assertNull(savedLog.getNewValue());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllLogsShouldReturnLogs() {

        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUser(user);
        log.setAction("CREATE");
        log.setEntityType("SUPPLIER");
        log.setEntityId(10L);
        log.setOldValue(null);
        log.setNewValue("{\"name\":\"Test Supplier\"}");
        log.setIpAddress("127.0.0.1");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(log));

        List<AuditLogResponse> responses =
                auditLogService.getAllLogs();

        assertEquals(1, responses.size());

        AuditLogResponse response = responses.get(0);

        assertEquals(1L, response.id());
        assertEquals(1L, response.userId());
        assertEquals("System Administrator", response.userName());
        assertEquals("CREATE", response.action());
        assertEquals("SUPPLIER", response.entityType());
        assertEquals(10L, response.entityId());
        assertEquals(
                "{\"name\":\"Test Supplier\"}",
                response.newValue()
        );
        assertEquals("127.0.0.1", response.ipAddress());
    }

    @Test
    void getLogsByEntityShouldConvertEntityTypeToUppercase() {

        AuditLog log = new AuditLog();
        log.setId(2L);
        log.setUser(user);
        log.setAction("UPDATE");
        log.setEntityType("INVOICE");
        log.setEntityId(100L);

        when(auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        "INVOICE",
                        100L
                ))
                .thenReturn(List.of(log));

        List<AuditLogResponse> responses =
                auditLogService.getLogsByEntity("invoice", 100L);

        assertEquals(1, responses.size());
        assertEquals("UPDATE", responses.get(0).action());
        assertEquals("INVOICE", responses.get(0).entityType());

        verify(auditLogRepository)
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        "INVOICE",
                        100L
                );
    }

    @Test
    void getLogsByUserShouldReturnMatchingLogs() {

        AuditLog log = new AuditLog();
        log.setId(3L);
        log.setUser(user);
        log.setAction("DELETE");
        log.setEntityType("SUPPLIER");
        log.setEntityId(20L);

        when(auditLogRepository
                .findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(log));

        List<AuditLogResponse> responses =
                auditLogService.getLogsByUser(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).userId());
        assertEquals("DELETE", responses.get(0).action());

        verify(auditLogRepository)
                .findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void logCurrentUserShouldCaptureAuthenticatedUserAndIp()
            throws Exception {

        AppUserDetails principal = new AppUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("192.168.1.10");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        auditLogService.logCurrentUser(
                "APPROVE",
                "INVOICE",
                100L,
                Map.of("status", "UNDER_REVIEW"),
                Map.of("status", "APPROVED")
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertEquals(user, savedLog.getUser());
        assertEquals("APPROVE", savedLog.getAction());
        assertEquals("INVOICE", savedLog.getEntityType());
        assertEquals(100L, savedLog.getEntityId());
        assertEquals("192.168.1.10", savedLog.getIpAddress());
        assertTrue(savedLog.getOldValue().contains("UNDER_REVIEW"));
        assertTrue(savedLog.getNewValue().contains("APPROVED"));
    }

    @Test
    void logCurrentUserShouldHandleMissingAuthentication() {

        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();

        auditLogService.logCurrentUser(
                "SYSTEM_ACTION",
                "INVOICE",
                100L,
                null,
                null
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertNull(savedLog.getUser());
        assertNull(savedLog.getIpAddress());
        assertEquals("SYSTEM_ACTION", savedLog.getAction());

        verifyNoInteractions(userRepository);
    }
}