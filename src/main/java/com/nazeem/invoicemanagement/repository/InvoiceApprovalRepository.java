package com.nazeem.invoicemanagement.repository;

import com.nazeem.invoicemanagement.entity.ApprovalDecision;
import com.nazeem.invoicemanagement.entity.InvoiceApproval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceApprovalRepository
        extends JpaRepository<InvoiceApproval, Long> {

    List<InvoiceApproval> findByInvoiceId(Long invoiceId);

    List<InvoiceApproval> findByApproverId(Long approverId);

    List<InvoiceApproval> findByDecision(ApprovalDecision decision);

    Optional<InvoiceApproval> findByInvoiceIdAndApproverId(
            Long invoiceId,
            Long approverId
    );

    boolean existsByInvoiceIdAndDecision(
            Long invoiceId,
            ApprovalDecision decision
    );
}