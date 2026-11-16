
package com.nazeem.invoicemanagement.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userName,
        String action,
        String entityType,
        Long entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        LocalDateTime createdAt
) {
}
