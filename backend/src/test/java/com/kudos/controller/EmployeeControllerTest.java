package com.kudos.controller;

import com.kudos.model.Employee;
import com.kudos.service.EmployeeService;
import com.kudos.service.KudosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private KudosService kudosService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockEmployee = new Employee();
        mockEmployee.setEmail("john@kudos.com");
        mockEmployee.setPassword("encodedPass");
    }

    // ---------- REGISTER ----------

    @Test
    void testRegister_Success() {
        when(employeeService.register(any(Employee.class))).thenReturn(mockEmployee);

        ResponseEntity<?> response = employeeController.register(mockEmployee);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockEmployee, response.getBody());

        verify(employeeService, times(1)).register(mockEmployee);
    }

    // ---------- LOGIN ----------

    @Test
    void testLogin_Success() {
        when(employeeService.login("john@kudos.com", "password123")).thenReturn("mocked-jwt-token");

        ResponseEntity<Map<String, String>> response = employeeController.login("john@kudos.com", "password123");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("mocked-jwt-token", response.getBody().get("token"));

        verify(employeeService, times(1)).login("john@kudos.com", "password123");
    }

    // ---------- GET PROFILE ----------
    @Test
    void testGetProfile_Found() {
        when(employeeService.findByEmail("john@kudos.com")).thenReturn(Optional.of(mockEmployee));

        ResponseEntity<EmployeeController.EmployeeProfileDTO> response = employeeController
                .getProfile("john@kudos.com");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        var profile = response.getBody();
        assertEquals(mockEmployee.getName(), profile.employee().name());
        assertEquals(mockEmployee.getEmail(), profile.employee().email());
        assertEquals(mockEmployee.getDepartment(), profile.employee().department());
    }

    @Test
    void testGetProfile_NotFound() {
        when(employeeService.findByEmail("nonexistent@kudos.com")).thenReturn(Optional.empty());

        ResponseEntity<EmployeeController.EmployeeProfileDTO> response = employeeController
                .getProfile("nonexistent@kudos.com");

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

}
