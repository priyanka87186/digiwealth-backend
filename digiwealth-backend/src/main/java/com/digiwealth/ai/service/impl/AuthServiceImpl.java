package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.request.LoginRequest;
import com.digiwealth.ai.dto.request.RegisterRequest;
import com.digiwealth.ai.dto.response.AuthResponse;
import com.digiwealth.ai.entity.Account;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.BadRequestException;
import com.digiwealth.ai.repository.AccountRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.security.JwtUtil;
import com.digiwealth.ai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        // Auto-create a default account for the new customer.
        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .build();
        accountRepository.save(account);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private String generateAccountNumber() {
        return "DW" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
