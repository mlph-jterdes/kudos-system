package com.kudos.service;

import com.kudos.model.Employee;
import com.kudos.repository.EmployeeRepository;
import com.kudos.security.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---------------- LOGIN ----------------
    public String login(String email, String rawPassword) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!passwordEncoder.matches(rawPassword, employee.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String role = "ROLE_EMPLOYEE"; // define role here
        return jwtUtil.generateToken(employee.getEmail(), role);
    }

    // ---------------- LOGIN CHECK PASSWORD ----------------
    public boolean checkPassword(Employee employee, String rawPassword) {
        return passwordEncoder.matches(rawPassword, employee.getPassword());
    }

    // ---------------- FIND BY EMAIL ----------------
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    // ---------------- REGISTER EMPLOYEE ----------------
    public Employee register(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    // ---------------- CHANGE PASSWORD ----------------
    public void changePassword(String email, String oldPassword, String newPassword) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }

}
