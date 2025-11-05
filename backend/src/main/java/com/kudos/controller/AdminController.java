package com.kudos.controller;

import com.kudos.dto.EmployeeDTO;
import com.kudos.model.Admin;
import com.kudos.model.Employee;
import com.kudos.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin API", description = "Admin operations for kudos system")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
@PreAuthorize("hasRole('ADMIN')") // All endpoints require ADMIN role unless overridden
public class AdminController {

    private final AdminService adminService;

    // ---------------- REGISTER ADMIN (One-time setup) ----------------
    @Operation(summary = "Register admin", description = "One-time setup for first admin", security = {})
    @PostMapping("/register") // Create account/user
    @PreAuthorize("permitAll()") // Public for first admin registration
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            Admin admin = adminService.createAdmin(email, password);
            return ResponseEntity
                    .ok(Map.of("message", "First admin registered successfully", "adminEmail", admin.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- LOGIN ----------------
    @Operation(summary = "Login admin", description = "Returns JWT token", security = {})
    @PostMapping("/login")
    @PreAuthorize("permitAll()") // Public login endpoint
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            // Login returns JWT token
            String token = adminService.login(email, password);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- UPLOAD EMPLOYEES (CSV | JWT required) ----------------
    @Operation(summary = "Upload employees via CSV", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadEmployees(
            @Parameter(description = "CSV file to upload") @RequestParam("file") MultipartFile file) {
        try {
            List<String> messages = adminService.uploadEmployeesFromCSV(file);

            return ResponseEntity.ok(Map.of("message", "Upload complete", "details", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- RESET KUDOS COUNTS (JWT required) ----------------
    @Operation(summary = "Reset kudos counts", description = "JWT required", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/reset-kudos")
    public ResponseEntity<?> resetKudos() {
        adminService.resetKudosCounts();
        return ResponseEntity.ok(Map.of("message", "All kudos counts reset."));
    }

    // ---------------- GET ALL EMPLOYEES (JWT required) ----------------
    @Operation(summary = "Get all employees", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = adminService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // ---------------- CREATE EMPLOYEE (JWT required) ----------------
    @Operation(summary = "Create employee", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/employees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEmployee(@RequestBody Employee emp) {
        try {
            Employee created = adminService.createEmployee(emp);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- RESET EMPLOYEE PASSWORD (JWT required) ----------------
    @Operation(summary = "Reset employee password", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/employees/{employeeId}/reset-password")
    public ResponseEntity<String> resetEmployeePassword(@PathVariable String employeeId) {
        adminService.adminResetEmployeePassword(employeeId);
        return ResponseEntity.ok("Password reset to default successfully");
    }
}
