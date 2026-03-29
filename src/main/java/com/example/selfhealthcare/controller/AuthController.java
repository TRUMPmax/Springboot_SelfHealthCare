package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.AuthLoginRequest;
import com.example.selfhealthcare.dto.AuthRegisterRequest;
import com.example.selfhealthcare.dto.AuthSessionResponse;
import com.example.selfhealthcare.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public AuthSessionResponse currentSession() {
        return authService.currentSession();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthSessionResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthSessionResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        authService.logout();
    }
}
