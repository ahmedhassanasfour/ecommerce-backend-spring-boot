package com.ahmed.ecommerce.auth.controller;

import com.ahmed.ecommerce.auth.dto.ApiResponse;
import com.ahmed.ecommerce.auth.dto.AuthResponse;
import com.ahmed.ecommerce.auth.dto.LoginRequest;
import com.ahmed.ecommerce.auth.dto.RegisterRequest;
import com.ahmed.ecommerce.auth.service.AuthService;
import com.ahmed.ecommerce.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);

        return new ApiResponse(
                "User registered successfully"
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            throw new UnauthorizedException(
                    "Refresh token is required"
            );
        }

        String refreshToken =
                authorizationHeader.substring(7).trim();

        if (refreshToken.isBlank()) {
            throw new UnauthorizedException(
                    "Refresh token is required"
            );
        }

        return authService.refreshToken(refreshToken);
    }
}