package com.kudos.config;

import com.kudos.model.Admin;
import com.kudos.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Inject values from application.properties
    @Value("${admin.default.email}")
    private String defaultEmail;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail(defaultEmail).isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail(defaultEmail);
            admin.setPassword(passwordEncoder.encode(defaultPassword));

            adminRepository.save(admin);
            System.out.println("[INFO] Default admin created: " + defaultEmail);
        } else {
            System.out.println("[INFO] Admin already exists: " + defaultEmail);
        }
    }
}
