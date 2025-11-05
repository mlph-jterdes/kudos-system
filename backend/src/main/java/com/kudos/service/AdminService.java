package com.kudos.service;

import com.kudos.dto.EmployeeDTO;
import com.kudos.model.Admin;
import com.kudos.model.Employee;
import com.kudos.model.Role;
import com.kudos.model.Team;
import com.kudos.repository.AdminRepository;
import com.kudos.repository.EmployeeRepository;
import com.kudos.repository.RoleRepository;
import com.kudos.repository.TeamRepository;
import com.kudos.security.JwtUtil;
import com.opencsv.CSVReader;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.kudos.dto.EmployeeDTO;
import com.kudos.model.Employee;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final JwtUtil jwtUtil;

    // Use a single BCrypt encoder for all password operations
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---------------- LOGIN ----------------
    // Returns JWT token if successful, throws RuntimeException if failed
    public String login(String email, String rawPassword) {
        Admin admin = adminRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String role = "ROLE_ADMIN";
        return jwtUtil.generateToken(admin.getEmail(), role);
    }

    // ---------------- LOGIN CHECK PASSWORD ----------------
    public boolean checkPassword(Admin admin, String rawPassword) {
        return passwordEncoder.matches(rawPassword, admin.getPassword());
    }

    // ---------------- REGISTER FIRST ADMIN (one-time only) ----------------
    public Admin createAdmin(String email, String password) {
        // Check if any admins already exist
        if (adminRepository.count() > 0) {
            throw new RuntimeException("First admin has already been registered!");
        }

        // Check ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        // Create new admin
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.getRoles().add(adminRole);

        return adminRepository.save(admin);
    }

    // ---------------- FIND ADMIN BY EMAIL ----------------
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    // ---------------- UPLOAD EMPLOYEES FROM CSV ----------------
    public List<String> uploadEmployeesFromCSV(MultipartFile file) {
        List<String> messages = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String[] line;
            boolean firstLine = true;

            while ((line = reader.readNext()) != null) {
                if (firstLine) { // skip header
                    firstLine = false;
                    continue;
                }

                if (line.length < 4) { // must have at least employeeId, name, email, department
                    messages.add("Skipping incomplete row: " + Arrays.toString(line));
                    continue;
                }

                String employeeId = line[0].trim();
                String name = line[1].trim();
                String email = line[2].trim();
                String department = line[3].trim();
                String teamsStr = line.length >= 5 ? line[4].trim() : "";
                Set<Team> teamSet = new HashSet<>();

                // Prevent duplicates
                if (employeeRepository.existsByEmployeeId(employeeId) || employeeRepository.existsByEmail(email)) {
                    messages.add("Duplicate skipped: " + name);
                    continue;
                }

                if (!teamsStr.isEmpty()) {
                    String[] teamNames = teamsStr.split(";"); // use semicolon as separator
                    for (String tName : teamNames) {
                        String trimmedName = tName.trim(); // final/effectively final
                        if (trimmedName.isEmpty())
                            continue;

                        // Find team in DB, create if not exists
                        Team team = teamRepository.findByName(trimmedName)
                                .orElseGet(() -> teamRepository.save(new Team(trimmedName)));

                        teamSet.add(team);
                    }
                }

                Employee emp = new Employee();
                emp.setEmployeeId(employeeId);
                emp.setName(name);
                emp.setEmail(email);
                emp.setDepartment(department.toLowerCase());
                emp.setTeams(teamSet); // if you have a field for teams
                emp.setKudosCount(0);
                emp.setPassword(passwordEncoder.encode(employeeId));
                employeeRepository.save(emp);
                messages.add("Added: " + name);
            }

        } catch (Exception e) {
            messages.add("Error: " + e.getMessage());
        }

        return messages;
    }

    // ---------------- RESET KUDOS ----------------
    public void resetKudosCounts() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            emp.setKudosCount(0);
        }
        employeeRepository.saveAll(employees);
    }

    // ---------------- GET ALL EMPLOYEES ----------------
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream().map(emp -> new EmployeeDTO(emp.getId(), emp.getEmployeeId(),
                emp.getName(), emp.getEmail(), emp.getDepartment())).collect(Collectors.toList());
    }

    // ---------------- CREATE EMPLOYEE ----------------
    public Employee createEmployee(Employee emp) {
        if (employeeRepository.existsByEmployeeId(emp.getEmployeeId())
                || employeeRepository.existsByEmail(emp.getEmail())) {
            throw new RuntimeException("Employee already exists!");
        }
        emp.setKudosCount(0); // Initialize kudos count
        emp.setDepartment(emp.getDepartment().toLowerCase());

        // Generate default password: first 3 letters of name + employeeId
        String defaultPassword = emp.getName().substring(0, Math.min(3, emp.getName().length())) + emp.getEmployeeId();
        emp.setPassword(passwordEncoder.encode(defaultPassword));

        return employeeRepository.save(emp);
    }

    // ---------------- RESET EMPLOYEE PASSWORD ----------------
    public void adminResetEmployeePassword(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Default password: first 3 letters of name + employeeId
        String defaultPassword = employee.getName().substring(0, Math.min(3, employee.getName().length()))
                + employee.getEmployeeId();

        employee.setPassword(passwordEncoder.encode(defaultPassword));
        employeeRepository.save(employee);
    }

}
