package com.bosshi.maeul.admin;

import com.bosshi.maeul.admin.entity.AdminUser;
import com.bosshi.maeul.admin.repository.AdminUserRepository;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import com.kraftadmin.security.AdminUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import security.SpringSecurityAdapter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminAuthIntegrationTest {

    @Autowired
    private SpringSecurityAdapter springSecurityAdapter;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 1. AdminUser 등록
        AdminUser admin = new AdminUser();
        admin.setEmail("admin@maeul.com");
        admin.setPassword(passwordEncoder.encode("admin1234"));
        adminUserRepository.save(admin);

        // 2. 일반 User 등록
        User user = new User();
        user.setEmail("user@maeul.com");
        user.setPassword(passwordEncoder.encode("user1234"));
        userRepository.save(user);
    }

    @Test
    @DisplayName("AdminUser 정보로 KraftAdmin 인증 성공")
    void adminUser_authentication_success() {
        AdminUserDTO dto = springSecurityAdapter.authenticateCredentials("admin@maeul.com", "admin1234");

        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("admin@maeul.com");
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("일반 User 정보로는 KraftAdmin 인증 실패")
    void regularUser_cannot_authenticate_as_admin() {
        AdminUserDTO dto = springSecurityAdapter.authenticateCredentials("user@maeul.com", "user1234");

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 인증 시 실패")
    void adminUser_wrong_password_fails() {
        AdminUserDTO dto = springSecurityAdapter.authenticateCredentials("admin@maeul.com", "wrongpassword");

        assertThat(dto).isNull();
    }
}
