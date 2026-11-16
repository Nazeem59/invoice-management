
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.audit.AuditLogResponse;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.AuditLogService;
import com.nazeem.invoicemanagement.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> getAllLogs(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        requireAdmin(user);
        return auditLogService.getAllLogs();
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public List<AuditLogResponse> getLogsByEntity(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {
        requireAdmin(user);
        return auditLogService.getLogsByEntity(entityType, entityId);
    }

    @GetMapping("/user/{userId}")
    public List<AuditLogResponse> getLogsByUser(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable Long userId
    ) {
        requireAdmin(user);
        return auditLogService.getLogsByUser(userId);
    }

    private void requireAdmin(AppUserDetails user) {

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        String currentRole = user.getRole();

        if (!UserRole.ADMIN.name().equalsIgnoreCase(currentRole)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can access audit logs"
            );
        }
    }
}
