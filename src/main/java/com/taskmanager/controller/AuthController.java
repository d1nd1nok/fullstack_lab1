package com.taskmanager.controller;

import com.taskmanager.security.JwtUtil;
import com.taskmanager.entity.User;
import com.taskmanager.dto.*;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.UserService;
import com.taskmanager.exception.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, UserService userService,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Имя пользователя уже существует: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email уже существует: " + request.getEmail());
        }

        UserDTO userDTO = userService.createUser(request);

        String token = jwtUtil.generateToken(request.getUsername());

        AuthResponse response = new AuthResponse(
                token,
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new ValidationException("Неверный пароль");
        }

        String token = jwtUtil.generateToken(username);

        AuthResponse response = new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getId()
        );

        return ResponseEntity.ok(response);
    }
}