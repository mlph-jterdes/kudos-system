package com.kudos.controller;

import com.kudos.model.Admin;
import com.kudos.model.Employee;
import com.kudos.security.JwtUtil;
import com.kudos.service.AdminService;
import com.kudos.service.EmployeeService;
import com.kudos.repository.AdminRepository;
import com.kudos.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AdminService adminService;
    private final EmployeeService employeeService;
    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // Try Admin first
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            if (passwordEncoder.matches(password, admin.getPassword())) {
                String token = jwtUtil.generateToken(email, "ADMIN");
                return ResponseEntity.ok(Map.of("token", token, "role", "ROLE_ADMIN"));
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid password"));
            }
        }

        // Try Employee
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent()) {
            Employee employee = empOpt.get();

            if (passwordEncoder.matches(password, employee.getPassword())) {
                String token = jwtUtil.generateToken(email, "EMPLOYEE");
                return ResponseEntity.ok(Map.of("token", token, "role", "ROLE_EMPLOYEE"));
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid password"));
            }
        }

        return ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }
}
