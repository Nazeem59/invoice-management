
package com.nazeem.invoicemanagement.dto;

import com.nazeem.invoicemanagement.entity.UserRole;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String fullName,
        String email,
        String role
) {}
