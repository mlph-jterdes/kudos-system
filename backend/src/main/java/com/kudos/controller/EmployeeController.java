package com.kudos.controller;

import com.kudos.model.Employee;
import com.kudos.model.Kudos;
import com.kudos.service.EmployeeService;
import com.kudos.service.KudosService;
import com.kudos.repository.EmployeeRepository;
import com.kudos.repository.TeamRepository;
import com.kudos.dto.EmployeeDTO;
import com.kudos.dto.TeamDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Tag(name = "Employee API", description = "Endpoints for employee login, profile, kudos, and comments")
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final KudosService kudosService;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    public record EmployeeProfileDTO(EmployeeDTO employee, List<TeamDTO> teams) {
    }

    // ---------------- REGISTER ----------------
    @Operation(summary = "Register new employee", description = "Public endpoint for employee registration")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Employee employee) {
        try {
            Employee created = employeeService.register(employee);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- LOGIN ----------------
    @Operation(summary = "Employee login", description = "Returns a JWT token")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password) {
        String token = employeeService.login(email, password);
        return ResponseEntity.ok(Map.of("token", token));
    }

    // ---------------- GET PROFILE ----------------
    @Operation(summary = "Get employee profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileDTO> getProfile(@RequestAttribute("email") String email) {
        return employeeService.findByEmail(email).map(emp -> {
            // Map teams to DTOs
            List<TeamDTO> teamDTOs = new ArrayList<>();
            if (emp.getTeams() != null && !emp.getTeams().isEmpty()) {
                for (var team : emp.getTeams()) {
                    teamDTOs.add(new TeamDTO(team.getId(), team.getName()));
                }
            }

            // Build profile DTO
            EmployeeProfileDTO profile = new EmployeeProfileDTO(new EmployeeDTO(emp.getId(), emp.getEmployeeId(),
                    emp.getName(), emp.getEmail(), emp.getDepartment()), teamDTOs);

            return ResponseEntity.ok(profile);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ---------------- GET ALL EMPLOYEES FOR KUDOS ----------------
    @GetMapping("/all")
    public List<Map<String, Object>> getAllEmployeesForKudos() {
        return employeeRepository.findAll().stream().map(emp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", emp.getId());
            map.put("employeeId", emp.getEmployeeId());
            map.put("email", emp.getEmail());
            map.put("name", emp.getName());
            map.put("department", emp.getDepartment());
            return map;
        }).toList();
    }

    // ---------------- GET ALL TEAMS FOR KUDOS ----------------
    @GetMapping("/teams")
    public List<Map<String, Object>> getAllTeamsForKudos() {
        return teamRepository.findAll().stream().map(team -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            return map;
        }).toList();
    }
}
