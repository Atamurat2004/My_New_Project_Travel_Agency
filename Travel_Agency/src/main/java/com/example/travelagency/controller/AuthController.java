package com.example.travelagency.controller;

import com.example.travelagency.model.User;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:8081", "http://192.168.56.1:8081", "http://192.168.43.7:8081"})
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        logger.info("Register request received for email: {} name: {} password: {}", email, name, password == null ? "null" : "***");
        try {
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                logger.warn("Registration failed: missing required fields. name={}, email={}, passwordEmpty={}", name, email, password == null || password.isEmpty());
                return ResponseEntity.badRequest().body(Map.of("error", "Все поля обязательны для заполнения"));
            }
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("Registration failed: email already exists: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Email уже зарегистрирован"));
            }
            String encodedPassword = passwordEncoder.encode(password);
            logger.info("Encoded password: {}", encodedPassword);
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(encodedPassword)
                    .role("ROLE_USER")
                    .build();
            User saved = userRepository.save(user);
            logger.info("User saved with id: {} email: {}", saved.getId(), saved.getEmail());
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
            String token = jwtService.generateToken(userDetails);
            logger.info("User registered successfully: {}", email);
            return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "role", "ROLE_USER"
            ));
        } catch (Exception e) {
            logger.error("Registration error for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при регистрации: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        logger.info("Login request received for email: {} password: {}", email, password == null ? "null" : "***");
        try {
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                logger.warn("Login failed: missing required fields. email={}, passwordEmpty={}", email, password == null || password.isEmpty());
                return ResponseEntity.badRequest().body(Map.of("error", "Email и пароль обязательны"));
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            logger.info("Login successful for user: {} with role: {}", email, user.getRole());
            return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "role", user.getRole()
            ));
        } catch (Exception e) {
            logger.error("Login failed for user: {}", email, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Ошибка входа: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        logger.info("Test endpoint called");
        try {
            long userCount = userRepository.count();
            logger.info("Total users in database: {}", userCount);
            
            List<Map<String, String>> users = userRepository.findAll().stream()
                    .map(user -> Map.of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "role", user.getRole()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "userCount", userCount,
                "users", users
            ));
        } catch (Exception e) {
            logger.error("Error in test endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Внутренняя ошибка сервера"));
        }
    }
} 