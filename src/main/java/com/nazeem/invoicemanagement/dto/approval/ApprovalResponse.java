package com.nazeem.invoicemanagement.dto.approval;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long id,
        Long invoiceId,
        Long approverId,
        String approverName,
        Integer approvalLevel,
        String decision,
        String comments,
        LocalDateTime actionDate
) {}