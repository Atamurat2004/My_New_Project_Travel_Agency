package com.example.travelagency.controller;

import com.example.travelagency.model.User;
import com.example.travelagency.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.travelagency.service.AuditLogService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping("/me")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null);
    }

    @PutMapping("/me")
    public User updateCurrentUser(@RequestBody User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }
        User user = userOpt.get();
        // Проверка email на уникальность, если он меняется
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new RuntimeException("Email уже занят другим пользователем");
            }
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        // Пароль меняем только если передан и не пустой
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getBirthDate() != null) {
            user.setBirthDate(updatedUser.getBirthDate());
        }
        if (updatedUser.getPassport() != null) {
            user.setPassport(updatedUser.getPassport());
        }
        if (updatedUser.getNotifications() != null) {
            user.setNotifications(updatedUser.getNotifications());
        }
        User saved = userRepository.save(user);
        auditLogService.logEntity("USER", saved.getId(), "UPDATE", saved);
        return saved;
    }
} 