package com.medibook.medibook.config;

import com.medibook.medibook.entity.User;
import com.medibook.medibook.enums.UserRole;
import com.medibook.medibook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@medibook.com";
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("Admin123!"));
                    admin.setIsActive(true);
                    userRepository.save(admin);
                    log.info("System administrator synchronized: {}", adminEmail);
                },
                () -> {
                    User admin = User.builder()
                            .fullName("System Administrator")
                            .email(adminEmail)
                            .password(passwordEncoder.encode("Admin123!"))
                            .phoneNumber("9999999999")
                            .role(UserRole.ADMIN)
                            .isActive(true)
                            .build();
                    userRepository.save(admin);
                    log.info("Default system administrator initialized: {}", adminEmail);
                }
        );
    }
}
