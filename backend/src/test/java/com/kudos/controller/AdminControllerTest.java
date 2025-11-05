package com.kudos.controller;

import com.kudos.model.Admin;
import com.kudos.security.JwtUtil;
import com.kudos.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AdminController adminController;

    private Admin mockAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockAdmin = new Admin();
        mockAdmin.setEmail("new@kudos.com"); // email to match test input
        mockAdmin.setPassword("encodedPass");
    }

    // ---------- REGISTER ADMIN ----------

    // @Test
    // void testRegister_Success() {
    // when(adminService.createAdmin("new@kudos.com",
    // "securePass")).thenReturn(mockAdmin);

    // ResponseEntity<?> response = adminController
    // .register(Map.of("email", "new@kudos.com", "password", "securePass"));

    // assertEquals(200, response.getStatusCodeValue());

    // @SuppressWarnings("unchecked")
    // Map<String, String> body = (Map<String, String>) response.getBody();
    // assertNotNull(body);
    // assertEquals("First admin registered successfully", body.get("message"));
    // assertEquals("new@kudos.com", body.get("adminEmail"));

    // verify(adminService, times(1)).createAdmin("new@kudos.com", "securePass");
    // }

    // @Test
    // void testRegister_Failure() {
    // when(adminService.createAdmin("new@kudos.com", "securePass"))
    // .thenThrow(new RuntimeException("First admin has already been registered!"));

    // ResponseEntity<?> response = adminController
    // .register(Map.of("email", "new@kudos.com", "password", "securePass"));

    // assertEquals(400, response.getStatusCodeValue());

    // @SuppressWarnings("unchecked")
    // Map<String, String> body = (Map<String, String>) response.getBody();
    // assertNotNull(body);
    // assertEquals("First admin has already been registered!", body.get("error"));

    // verify(adminService, times(1)).createAdmin("new@kudos.com", "securePass");
    // }

    // // ---------- LOGIN ----------

    // @Test
    // void testLogin_Success() {
    // when(adminService.login("admins@kudos.com",
    // "admin123")).thenReturn("mocked-jwt-token");

    // ResponseEntity<?> response = adminController.login(Map.of("email",
    // "admins@kudos.com", "password", "admin123"));

    // assertEquals(200, response.getStatusCodeValue());

    // @SuppressWarnings("unchecked")
    // Map<String, String> body = (Map<String, String>) response.getBody();
    // assertNotNull(body);
    // assertEquals("mocked-jwt-token", body.get("token"));

    // verify(adminService, times(1)).login("admins@kudos.com", "admin123");
    // }

    // @Test
    // void testLogin_Failure() {
    // when(adminService.login("admins@kudos.com", "wrongpass")).thenThrow(new
    // RuntimeException("Invalid password"));

    // ResponseEntity<?> response = adminController
    // .login(Map.of("email", "admins@kudos.com", "password", "wrongpass"));

    // assertEquals(401, response.getStatusCodeValue());

    // @SuppressWarnings("unchecked")
    // Map<String, String> body = (Map<String, String>) response.getBody();
    // assertNotNull(body);
    // assertEquals("Invalid password", body.get("error"));

    // verify(adminService, times(1)).login("admins@kudos.com", "wrongpass");
    // }

    // // ---------- UPLOAD EMPLOYEES ----------

    // @Test
    // void testUploadEmployees() throws Exception {
    // MockMultipartFile file = new MockMultipartFile("file", "employees.csv",
    // "text/csv",
    // "id,name,email,department,teams\n1,John,john@kudos.com,IT,TeamA".getBytes());

    // List<String> messages = List.of("Added: John");
    // when(adminService.uploadEmployeesFromCSV(file)).thenReturn(messages);

    // ResponseEntity<Map<String, Object>> response =
    // adminController.uploadEmployees(file);

    // assertEquals(200, response.getStatusCodeValue());
    // assertEquals(messages, response.getBody());

    // verify(adminService, times(1)).uploadEmployeesFromCSV(file);
    // }

    // // ---------- RESET KUDOS COUNTS ----------

    // @Test
    // void testResetKudos() {
    // doNothing().when(adminService).resetKudosCounts();

    // ResponseEntity<?> response = adminController.resetKudos();

    // assertEquals(200, response.getStatusCodeValue());

    // @SuppressWarnings("unchecked")
    // Map<String, String> body = (Map<String, String>) response.getBody();
    // assertNotNull(body);
    // assertEquals("All kudos counts reset.", body.get("message"));

    // verify(adminService, times(1)).resetKudosCounts();
    // }

    // // ---------- RESET EMPLOYEE PASSWORD ----------

    // @Test
    // void testResetEmployeePassword() {
    // doNothing().when(adminService).adminResetEmployeePassword("E002");

    // ResponseEntity<String> response =
    // adminController.resetEmployeePassword("E002");

    // assertEquals(200, response.getStatusCodeValue());
    // assertEquals("Password reset to default successfully", response.getBody());

    // verify(adminService, times(1)).adminResetEmployeePassword("E002");
    // }

}
