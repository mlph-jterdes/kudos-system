//package com.kudos.service;
//
//import com.kudos.model.Employee;
//import com.kudos.repository.EmployeeRepository;
//import com.kudos.security.JwtUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class EmployeeServiceTest {
//
//    @Mock
//    private EmployeeRepository employeeRepository;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @InjectMocks
//    private EmployeeService employeeService;
//
//    private Employee mockEmployee;
//
//    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        mockEmployee = new Employee();
//        mockEmployee.setEmail("john@kudos.com");
//        mockEmployee.setPassword(passwordEncoder.encode("password123"));
//    }
//
//    // ---------- LOGIN ----------
//
//    // @Test
//    // void testLogin_Success() {
//    // when(employeeRepository.findByEmail("john@kudos.com")).thenReturn(Optional.of(mockEmployee));
//    // String rawPassword = "password123";
//
//    // // Stub JWT generation with both parameters
//    // when(jwtUtil.generateToken("john@kudos.com",
//    // "ROLE_EMPLOYEE")).thenReturn("mocked-jwt-token");
//
//    // // Call the service method
//    // String token = employeeService.login("john@kudos.com", rawPassword);
//
//    // assertEquals("mocked-jwt-token", token);
//    // verify(employeeRepository, times(1)).findByEmail("john@kudos.com");
//
//    // // Verify JWT call with **both arguments**
//    // verify(jwtUtil, times(1)).generateToken("john@kudos.com", "ROLE_EMPLOYEE");
//    // }
//
//    // @Test
//    // void testLogin_InvalidPassword() {
//    // when(employeeRepository.findByEmail("john@kudos.com")).thenReturn(Optional.of(mockEmployee));
//
//    // RuntimeException exception = assertThrows(RuntimeException.class,
//    // () -> employeeService.login("john@kudos.com", "wrongpass"));
//
//    // assertEquals("Invalid credentials.", exception.getMessage());
//    // }
//
//    // @Test
//    // void testLogin_EmailNotFound() {
//    // when(employeeRepository.findByEmail("nonexistent@kudos.com")).thenReturn(Optional.empty());
//
//    // RuntimeException exception = assertThrows(RuntimeException.class,
//    // () -> employeeService.login("nonexistent@kudos.com", "password"));
//
//    // assertEquals("Employee not found.", exception.getMessage());
//    // }
//
//    // // ---------- REGISTER ----------
//
//    // @Test
//    // void testRegister_Success() {
//    // Employee toRegister = new Employee();
//    // toRegister.setEmail("new@kudos.com");
//    // toRegister.setPassword("mypassword");
//
//    // when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation ->
//    // invocation.getArgument(0));
//
//    // Employee created = employeeService.register(toRegister);
//
//    // assertNotNull(created);
//    // assertEquals("new@kudos.com", created.getEmail());
//    // assertNotEquals("mypassword", created.getPassword()); // password should be
//    // encoded
//    // verify(employeeRepository, times(1)).save(toRegister);
//    // }
//
//    // // ---------- CHANGE PASSWORD ----------
//
//    // @Test
//    // void testChangePassword_Success() {
//    // when(employeeRepository.findByEmail("john@kudos.com")).thenReturn(Optional.of(mockEmployee));
//
//    // employeeService.changePassword("john@kudos.com", "password123",
//    // "newpass123");
//
//    // assertTrue(passwordEncoder.matches("newpass123",
//    // mockEmployee.getPassword()));
//    // verify(employeeRepository, times(1)).save(mockEmployee);
//    // }
//
//    // @Test
//    // void testChangePassword_WrongOldPassword() {
//    // when(employeeRepository.findByEmail("john@kudos.com")).thenReturn(Optional.of(mockEmployee));
//
//    // RuntimeException exception = assertThrows(RuntimeException.class,
//    // () -> employeeService.changePassword("john@kudos.com", "wrongpass",
//    // "newpass"));
//
//    // assertEquals("Old password is incorrect", exception.getMessage());
//    // verify(employeeRepository, never()).save(any());
//    // }
//
//    // @Test
//    // void testChangePassword_EmployeeNotFound() {
//    // when(employeeRepository.findByEmail("nonexistent@kudos.com")).thenReturn(Optional.empty());
//
//    // RuntimeException exception = assertThrows(RuntimeException.class,
//    // () -> employeeService.changePassword("nonexistent@kudos.com", "any",
//    // "newpass"));
//
//    // assertEquals("Employee not found", exception.getMessage());
//    // }
//}
