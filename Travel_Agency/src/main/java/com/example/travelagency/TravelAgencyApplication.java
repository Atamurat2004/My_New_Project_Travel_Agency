package com.example.travelagency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.travelagency.model.User;
import com.example.travelagency.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class TravelAgencyApplication {
    private static final Logger logger = LoggerFactory.getLogger(TravelAgencyApplication.class);
    
    public static void main(String[] args) {
        SpringApplication.run(TravelAgencyApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("Initializing data...");
            
            // Проверяем, есть ли уже админский пользователь
            if (userRepository.findByEmail("admin@agency.com").isEmpty()) {
                logger.info("Creating admin user...");
                User admin = User.builder()
                        .name("Admin")
                        .email("admin@agency.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                logger.info("Admin user created successfully");
            } else {
                logger.info("Admin user already exists");
            }
            
            long userCount = userRepository.count();
            logger.info("Total users in database: {}", userCount);
        };
    }
} 