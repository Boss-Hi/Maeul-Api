package com.bosshi.maeul.admin.seeder;

import com.bosshi.maeul.admin.entity.AdminUser;
import com.bosshi.maeul.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class AdminUserSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${kraftadmin.default-admin.email:admin@example.com}")
    private String defaultAdminEmail;

    @Value("${kraftadmin.default-admin.password:change-me}")
    private String defaultAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminUserRepository.existsByEmail(defaultAdminEmail)) {
            log.info("[AdminUserSeeder] Admin user already exists with email: {}", defaultAdminEmail);
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail(defaultAdminEmail);
        adminUser.setPassword(passwordEncoder.encode(defaultAdminPassword));

        adminUserRepository.save(adminUser);
        log.info("[AdminUserSeeder] Created default admin user with email: {}", defaultAdminEmail);
    }
}
