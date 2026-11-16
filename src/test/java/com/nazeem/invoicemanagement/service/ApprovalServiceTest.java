
package com.nazeem.invoicemanagement.service;

import com.nazeem.invoicemanagement.dto.approval.ApprovalResponse;
import com.nazeem.invoicemanagement.entity.ApprovalDecision;
import com.nazeem.invoicemanagement.entity.Invoice;
import com.nazeem.invoicemanagement.entity.InvoiceApproval;
import com.nazeem.invoicemanagement.entity.InvoiceItem;
import com.nazeem.invoicemanagement.entity.InvoiceStatus;
import com.nazeem.invoicemanagement.entity.User;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.repository.InvoiceApprovalRepository;
import com.nazeem.invoicemanagement.repository.InvoiceRepository;
import com.nazeem.invoicemanagement.repository.UserRepository;
import com.nazeem.invoicemanagement.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceApprovalRepository approvalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ApprovalService approvalService;

    private Invoice invoice;
    private User approver;
    private InvoiceApproval approval;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus(InvoiceStatus.DRAFT);

        approver = new User();
        approver.setId(2L);
        approver.setFullName("Invoice Approver");
        approver.setActive(true);

        approval = new InvoiceApproval();
        approval.setId(1L);
        approval.setInvoice(invoice);
        approval.setApprover(approver);
        approval.setApprovalLevel(1);
        approval.setDecision(ApprovalDecision.PENDING);
    }

    @Test
    void submitInvoiceShouldMoveStatusToUnderReview() {

        invoice.getItems().add(new InvoiceItem());

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(userRepository.findByRoleAndActiveTrue(UserRole.APPROVER))
                .thenReturn(List.of(approver));

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        approvalService.submitInvoice(1L);

        assertEquals(
                InvoiceStatus.UNDER_REVIEW,
                invoice.getStatus()
        );

        assertEquals(1, invoice.getApprovals().size());

        assertEquals(
                ApprovalDecision.PENDING,
                invoice.getApprovals().get(0).getDecision()
        );

        verify(invoiceRepository).save(invoice);

        verify(auditLogService).logCurrentUser(
                eq("SUBMIT"),
                eq("INVOICE"),
                eq(1L),
                anyMap(),
                anyMap()
        );
    }

    @Test
    void submitInvoiceShouldRejectNonDraftInvoice() {

        invoice.setStatus(InvoiceStatus.APPROVED);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.submitInvoice(1L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "Only DRAFT invoices can be submitted",
                exception.getReason()
        );

        verifyNoInteractions(userRepository);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void submitInvoiceShouldRejectInvoiceWithoutItems() {

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.submitInvoice(1L)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Invoice must contain at least one item",
                exception.getReason()
        );

        verifyNoInteractions(userRepository);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void submitInvoiceShouldRejectWhenNoActiveApprover() {

        invoice.getItems().add(new InvoiceItem());

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(userRepository.findByRoleAndActiveTrue(UserRole.APPROVER))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.submitInvoice(1L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "No active approver is available",
                exception.getReason()
        );

        verify(invoiceRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void approveInvoiceShouldApproveWhenAllApproversApproved() {

        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(approvalRepository.findByInvoiceIdAndApproverId(1L, 2L))
                .thenReturn(Optional.of(approval));

        when(approvalRepository.existsByInvoiceIdAndDecision(
                1L,
                ApprovalDecision.PENDING
        )).thenReturn(false);

        when(approvalRepository.save(any(InvoiceApproval.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalResponse response = approvalService.approveInvoice(
                1L,
                2L,
                null
        );

        assertEquals(
                ApprovalDecision.APPROVED.name(),
                response.decision()
        );

        assertEquals(
                InvoiceStatus.APPROVED,
                invoice.getStatus()
        );

        verify(approvalRepository).save(approval);
        verify(invoiceRepository).save(invoice);

        verify(auditLogService).logCurrentUser(
                eq("APPROVE"),
                eq("INVOICE"),
                eq(1L),
                anyMap(),
                anyMap()
        );
    }

    @Test
    void rejectInvoiceShouldMoveInvoiceToRejected() {

        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(approvalRepository.findByInvoiceIdAndApproverId(1L, 2L))
                .thenReturn(Optional.of(approval));

        when(approvalRepository.save(any(InvoiceApproval.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalResponse response = approvalService.rejectInvoice(
                1L,
                2L,
                null
        );

        assertEquals(
                ApprovalDecision.REJECTED.name(),
                response.decision()
        );

        assertEquals(
                InvoiceStatus.REJECTED,
                invoice.getStatus()
        );

        verify(approvalRepository).save(approval);
        verify(invoiceRepository).save(invoice);

        verify(auditLogService).logCurrentUser(
                eq("REJECT"),
                eq("INVOICE"),
                eq(1L),
                anyMap(),
                anyMap()
        );
    }

    @Test
    void processDecisionShouldRejectDuplicateDecision() {

        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);
        approval.setDecision(ApprovalDecision.APPROVED);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(approvalRepository.findByInvoiceIdAndApproverId(1L, 2L))
                .thenReturn(Optional.of(approval));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.approveInvoice(1L, 2L, null)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "You have already submitted your decision",
                exception.getReason()
        );

        verify(approvalRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void processDecisionShouldRejectUnauthorizedApprover() {

        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(invoice));

        when(approvalRepository.findByInvoiceIdAndApproverId(1L, 99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.approveInvoice(1L, 99L, null)
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        assertEquals(
                "You are not assigned to approve this invoice",
                exception.getReason()
        );

        verify(approvalRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
    }
}