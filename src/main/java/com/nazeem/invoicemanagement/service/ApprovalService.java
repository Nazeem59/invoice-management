
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.approval.ApprovalRequest;
import com.nazeem.invoicemanagement.dto.approval.ApprovalResponse;
import com.nazeem.invoicemanagement.entity.ApprovalDecision;
import com.nazeem.invoicemanagement.entity.Invoice;
import com.nazeem.invoicemanagement.entity.InvoiceApproval;
import com.nazeem.invoicemanagement.entity.InvoiceStatus;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.repository.InvoiceApprovalRepository;
import com.nazeem.invoicemanagement.repository.InvoiceRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ApprovalService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ApprovalService(
            InvoiceRepository invoiceRepository,
            InvoiceApprovalRepository approvalRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public void submitInvoice(Long invoiceId) {

        Invoice invoice = findInvoice(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only DRAFT invoices can be submitted"
            );
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invoice must contain at least one item"
            );
        }
        InvoiceStatus oldStatus = invoice.getStatus();
        List<User> approvers = userRepository.findByRoleAndActiveTrue(
                UserRole.APPROVER
        );

        if (approvers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No active approver is available"
            );
        }

        for (User approver : approvers) {

            InvoiceApproval approval = new InvoiceApproval();

            approval.setInvoice(invoice);
            approval.setApprover(approver);
            approval.setApprovalLevel(1);
            approval.setDecision(ApprovalDecision.PENDING);

            invoice.addApproval(approval);
        }

        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        auditLogService.logCurrentUser(
                "SUBMIT",
                "INVOICE",
                savedInvoice.getId(),
                Map.of(
                        "status", oldStatus.name()
                ),
                Map.of(
                        "status", savedInvoice.getStatus().name()
                )
        );
    }

    public ApprovalResponse approveInvoice(
            Long invoiceId,
            Long approverId,
            ApprovalRequest request) {

        return processDecision(
                invoiceId,
                approverId,
                ApprovalDecision.APPROVED,
                request
        );
    }

    public ApprovalResponse rejectInvoice(
            Long invoiceId,
            Long approverId,
            ApprovalRequest request) {

        return processDecision(
                invoiceId,
                approverId,
                ApprovalDecision.REJECTED,
                request
        );
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> getInvoiceApprovals(Long invoiceId) {

        findInvoice(invoiceId);

        return approvalRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ApprovalResponse processDecision(
            Long invoiceId,
            Long approverId,
            ApprovalDecision decision,
            ApprovalRequest request) {

        Invoice invoice = findInvoice(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invoice is not under review"
            );
        }

        InvoiceApproval approval = approvalRepository
                .findByInvoiceIdAndApproverId(invoiceId, approverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not assigned to approve this invoice"
                ));

        if (approval.getDecision() != ApprovalDecision.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already submitted your decision"
            );
        }
        ApprovalResponse oldApprovalValue = toResponse(approval);

        InvoiceStatus oldInvoiceStatus = invoice.getStatus();

        approval.setDecision(decision);
        approval.setComments(
                request == null ? null : request.comments()
        );
        approval.setActionDate(LocalDateTime.now());

        approvalRepository.save(approval);

        if (decision == ApprovalDecision.REJECTED) {

            invoice.setStatus(InvoiceStatus.REJECTED);

        } else {

            boolean pendingApprovals =
                    approvalRepository.existsByInvoiceIdAndDecision(
                            invoiceId,
                            ApprovalDecision.PENDING
                    );

            if (!pendingApprovals) {
                invoice.setStatus(InvoiceStatus.APPROVED);
            }
        }

        invoiceRepository.save(invoice);

        ApprovalResponse newApprovalValue = toResponse(approval);

        String action = decision == ApprovalDecision.APPROVED
                ? "APPROVE"
                : "REJECT";

        auditLogService.logCurrentUser(
                action,
                "INVOICE",
                invoiceId,
                Map.of(
                        "invoiceStatus", oldInvoiceStatus.name(),
                        "approval", oldApprovalValue
                ),
                Map.of(
                        "invoiceStatus", invoice.getStatus().name(),
                        "approval", newApprovalValue
                )
        );

        return newApprovalValue;
    }

    private Invoice findInvoice(Long id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found"
                ));
    }

    private ApprovalResponse toResponse(InvoiceApproval approval) {

        return new ApprovalResponse(
                approval.getId(),
                approval.getInvoice().getId(),
                approval.getApprover().getId(),
                approval.getApprover().getFullName(),
                approval.getApprovalLevel(),
                approval.getDecision().name(),
                approval.getComments(),
                approval.getActionDate()
        );
    }
}
