package com.nazeem.invoicemanagement.dto.approval;

import jakarta.validation.constraints.Size;

public record ApprovalRequest(

        @Size(max = 1000, message = "Comments maximum 1000 characters")
        String comments

) {}