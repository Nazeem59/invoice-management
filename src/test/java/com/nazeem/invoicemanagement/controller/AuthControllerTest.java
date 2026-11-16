
package com.nazeem.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazeem.invoicemanagement.dto.AuthRequest;
import com.nazeem.invoicemanagement.dto.AuthResponse;
import com.nazeem.invoicemanagement.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginShouldReturnAccessTokenWhenCredentialsAreValid() throws Exception {

        AuthRequest request = new AuthRequest(
                "admin@invoice.local",
                "AdminPassword123"
        );

        AuthResponse response = new AuthResponse(
                "test.jwt.access.token",
                "Bearer",
                900000L,
                1L,
                "System Administrator",
                "admin@invoice.local",
                "ADMIN"
        );

        when(authService.login(any(AuthRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("test.jwt.access.token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(900000))
                .andExpect(jsonPath("$.userId")
                        .value(1))
                .andExpect(jsonPath("$.email")
                        .value("admin@invoice.local"))
                .andExpect(jsonPath("$.role")
                        .value("ADMIN"));

        verify(authService).login(any(AuthRequest.class));
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {

        String request = """
                {
                    "email": "invalid-email",
                    "password": "AdminPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(AuthRequest.class));
    }

    @Test
    void loginShouldReturnBadRequestWhenPasswordIsBlank() throws Exception {

        String request = """
                {
                    "email": "admin@invoice.local",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(AuthRequest.class));
    }
}
