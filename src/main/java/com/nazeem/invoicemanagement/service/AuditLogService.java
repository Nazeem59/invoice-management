
package com.nazeem.invoicemanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazeem.invoicemanagement.dto.audit.AuditLogResponse;
import com.nazeem.invoicemanagement.entity.AuditLog;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.repository.AuditLogRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void log(
            Long userId,
            String action,
            String entityType,
            Long entityId,
            Object oldValue,
            Object newValue,
            String ipAddress
    ) {
        AuditLog auditLog = new AuditLog();

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            auditLog.setUser(user);
        }

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValue(toJson(oldValue));
        auditLog.setNewValue(toJson(newValue));
        auditLog.setIpAddress(ipAddress);

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        List<AuditLog> logs =
                auditLogRepository.findAllByOrderByCreatedAtDesc();

        return logs.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(
            String entityType,
            Long entityId
    ) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        entityType.toUpperCase(),
                        entityId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize audit log data",
                    e
            );
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUser() != null ? log.getUser().getFullName() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }

    public void logCurrentUser(
            String action,
            String entityType,
            Long entityId,
            Object oldValue,
            Object newValue
    ) {
        Long userId = null;
        String ipAddress = null;

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof AppUserDetails user) {
            userId = user.getId();
        }

        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {

            ipAddress = attributes.getRequest().getRemoteAddr();
        }

        log(
                userId,
                action,
                entityType,
                entityId,
                oldValue,
                newValue,
                ipAddress
        );
    }
}
