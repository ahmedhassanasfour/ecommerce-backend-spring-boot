package com.ahmed.ecommerce.auth.service;

import com.ahmed.ecommerce.auth.dto.ApiResponse;
import com.ahmed.ecommerce.auth.dto.AuthResponse;
import com.ahmed.ecommerce.auth.dto.LoginRequest;
import com.ahmed.ecommerce.auth.dto.RegisterRequest;
import com.ahmed.ecommerce.auth.security.JwtService;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.exception.UnauthorizedException;
import com.ahmed.ecommerce.user.entity.Role;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public ApiResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        return new ApiResponse("User registered successfully");

    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken =
                jwtService.generateAccessToken(request.getEmail());

        String refreshToken =
                jwtService.generateRefreshToken(request.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }


    public AuthResponse refreshToken(String refreshToken) {

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("User is disabled");
        }

        String newAccessToken =
                jwtService.generateAccessToken(email);

        String newRefreshToken =
                jwtService.generateRefreshToken(email);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken
        );
    }
}