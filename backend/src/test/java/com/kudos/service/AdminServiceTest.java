package com.kudos.service;

import com.kudos.model.Admin;
import com.kudos.model.Role;
import com.kudos.repository.AdminRepository;
import com.kudos.repository.RoleRepository;
import com.kudos.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtil jwtUtil;

    // Use real encoder, no @Spy needed
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminService = new AdminService(adminRepository, null, roleRepository, null, jwtUtil);
    }

    // ---------------- LOGIN SUCCESS ----------------
    @Test
    void testLogin_Success() {
        String email = "admin@example.com";
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(encodedPassword);
        admin.setRoles(new HashSet<>()); // initialize roles

        when(adminRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(jwtUtil.generateToken(email, "ROLE_ADMIN")).thenReturn("fake-jwt-token");

        String token = adminService.login(email, rawPassword);
        assertEquals("fake-jwt-token", token);
    }

    // ---------------- LOGIN WRONG PASSWORD ----------------
    @Test
    void testLogin_WrongPassword() {
        String email = "admin@example.com";
        String correctPassword = "admin123";
        String wrongPassword = "wrongpass";

        String encodedPassword = passwordEncoder.encode(correctPassword);

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(encodedPassword);
        admin.setRoles(new HashSet<>());

        when(adminRepository.findByEmail(email)).thenReturn(Optional.of(admin));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.login(email, wrongPassword));
        assertEquals("Invalid password", ex.getMessage());
    }

    // // ---------------- CREATE ADMIN SUCCESS ----------------
    // @Test
    // void testCreateAdmin_Success() {
    // String email = "firstadmin@example.com";
    // String password = "admin123";

    // Role adminRole = new Role();
    // adminRole.setName("ADMIN");

    // when(adminRepository.count()).thenReturn(0L);
    // when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

    // // Mock saved admin with initialized roles
    // Admin savedAdmin = new Admin();
    // savedAdmin.setEmail(email);
    // savedAdmin.setPassword("encoded-password");
    // savedAdmin.setRoles(new HashSet<>());

    // when(adminRepository.save(any(Admin.class))).thenReturn(savedAdmin);

    // // ⚡ Use doReturn for spy
    // doReturn("encoded-password").when(passwordEncoder).encode(password);

    // Admin result = adminService.createAdmin(email, password);

    // assertNotNull(result);
    // assertEquals(email, result.getEmail());
    // assertTrue(result.getRoles().contains(adminRole));

    // verify(passwordEncoder, times(1)).encode(password);
    // verify(adminRepository, times(1)).save(any(Admin.class));
    // }

    // // ---------------- CREATE ADMIN FAILURE: already exists ----------------
    // @Test
    // void testCreateAdmin_Failure_AlreadyExists() {
    // when(adminRepository.count()).thenReturn(1L);

    // RuntimeException ex = assertThrows(RuntimeException.class,
    // () -> adminService.createAdmin("email", "pass"));

    // assertEquals("First admin has already been registered!", ex.getMessage());
    // }
}
