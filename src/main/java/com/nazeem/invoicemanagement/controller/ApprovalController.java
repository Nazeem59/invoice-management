
package com.nazeem.invoicemanagement.controller;

import com.nazeem.invoicemanagement.dto.approval.ApprovalRequest;
import com.nazeem.invoicemanagement.dto.approval.ApprovalResponse;
import com.nazeem.invoicemanagement.entity.UserRole;
import com.nazeem.invoicemanagement.security.AppUserDetails;
import com.nazeem.invoicemanagement.service.ApprovalService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/{id}/submit")
    public Map<String, String> submitInvoice(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user) {

        requireRole(user, UserRole.FINANCE, UserRole.ADMIN);

        approvalService.submitInvoice(id);

        return Map.of(
                "message", "Invoice submitted successfully",
                "status", "UNDER_REVIEW"
        );
    }

    @PostMapping("/{id}/approve")
    public ApprovalResponse approveInvoice(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal AppUserDetails user) {

        requireRole(user, UserRole.APPROVER);

        return approvalService.approveInvoice(
                id,
                user.getId(),
                request
        );
    }

    @PostMapping("/{id}/reject")
    public ApprovalResponse rejectInvoice(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal AppUserDetails user) {

        requireRole(user, UserRole.APPROVER);

        return approvalService.rejectInvoice(
                id,
                user.getId(),
                request
        );
    }

    @GetMapping("/{id}/approvals")
    public List<ApprovalResponse> getInvoiceApprovals(
            @PathVariable Long id) {

        return approvalService.getInvoiceApprovals(id);
    }

    private void requireRole(
            AppUserDetails user,
            UserRole... allowedRoles) {

        if (user == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        for (UserRole role : allowedRoles) {
            if (user.getRole().equalsIgnoreCase(role.name())) {
                return;
            }
        }

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action"
        );
    }
}
