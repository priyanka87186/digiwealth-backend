package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.request.LoginRequest;
import com.digiwealth.ai.dto.request.RegisterRequest;
import com.digiwealth.ai.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
