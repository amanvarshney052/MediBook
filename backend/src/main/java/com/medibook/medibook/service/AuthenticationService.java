package com.medibook.medibook.service;

import com.medibook.medibook.dto.request.LoginRequest;
import com.medibook.medibook.dto.request.RegisterPatientRequest;
import com.medibook.medibook.dto.response.AuthResponse;
import com.medibook.medibook.dto.response.UserResponse;
import com.medibook.medibook.entity.User;
import com.medibook.medibook.enums.UserRole;
import com.medibook.medibook.exception.InvalidCredentialsException;
import com.medibook.medibook.exception.ResourceAlreadyExistsException;
import com.medibook.medibook.exception.ResourceNotFoundException;
import com.medibook.medibook.mapper.UserMapper;
import com.medibook.medibook.repository.UserRepository;
import com.medibook.medibook.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse registerPatient(RegisterPatientRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Verify email uniqueness
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("Email " + normalizedEmail + " is already registered");
        }

        // 2. Create User entity strictly with PATIENT role (client cannot choose role)
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.PATIENT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // 3. Generate JWT access token
        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        return userMapper.toAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Authenticate credentials via Spring Security AuthenticationManager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 2. Load user details
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // 3. Verify active status
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("Your account has been deactivated. Please contact support.");
        }

        // 4. Generate JWT access token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );

        return userMapper.toAuthResponse(user, token);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    public void logout(String token) {
        if (token != null) {
            jwtService.revokeToken(token);
        }
    }
}
