package com.kudos.controller;

import com.kudos.model.Employee;
import com.kudos.model.Kudos;
import com.kudos.model.Team;
import com.kudos.service.KudosService;
import com.kudos.service.EmployeeService;
import com.kudos.dto.KudosDTO;
import com.kudos.repository.EmployeeRepository;
import com.kudos.repository.TeamRepository;
import com.kudos.repository.KudosRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

@Tag(name = "Kudos API", description = "Endpoints for sending kudos and comments")
@RestController
@RequestMapping("/api/kudos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class KudosController {

    private final KudosService kudosService;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final KudosRepository kudosRepository;

    // ---------------- PUBLIC SEARCH: HISTORY ----------------
    @Operation(summary = "Public history - Kudos/comments for employee or team", description = "Lists kudos/comments received by a specific employee or team with optional period filtering and message limit.", tags = {
            "Public Search" })
    @GetMapping("/public/history")
    public ResponseEntity<?> getPublicHistory(@RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId, @RequestParam(defaultValue = "all") String period,
            @RequestParam(defaultValue = "false") boolean showAllMessages) {

        if (employeeId == null && teamId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "employeeId or teamId is required"));
        }

        return ResponseEntity.ok(kudosService.getPublicHistory(employeeId, teamId, period, showAllMessages));
    }

    // ---------------- GET TOTAL KUDOS COUNT ----------------
    @GetMapping("/employee/{employeeId}/kudos-count")
    public ResponseEntity<?> getEmployeeKudosCount(@PathVariable Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);

        if (employeeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Employee not found"));
        }

        Employee employee = employeeOpt.get();
        int count = employee.getKudosCount();

        return ResponseEntity
                .ok(Map.of("employeeId", employee.getEmployeeId(), "name", employee.getName(), "totalKudos", count));
    }

    // ---------------- SEND KUDOS ----------------
    @Operation(summary = "Send kudos", description = "Authenticated employees can send kudos to another employee or a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/send")
    public ResponseEntity<?> sendKudos(@RequestAttribute("email") String senderEmail, // from JWT
            @RequestBody KudosDTO dto) {
        try {
            Employee sender = employeeService.findByEmail(senderEmail)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            Kudos sent = kudosService.sendKudos(sender.getEmployeeId(), dto.message(), dto.recipientEmployeeId(),
                    dto.recipientTeamId(), dto.anonymous());

            return ResponseEntity.ok(Map.of("message", "Kudos sent successfully!", "data", sent));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------- LEAVE COMMENT ----------------
    @PostMapping("/comment")
    @Operation(summary = "Send a comment to an employee or team", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> sendComment(@RequestAttribute("email") String email, @RequestBody KudosDTO dto) {

        var senderOpt = employeeService.findByEmail(email);
        if (senderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid sender."));
        }

        Employee sender = senderOpt.get();

        Kudos kudos = Kudos.builder()
                .recipientEmployee(dto.recipientEmployeeId() != null
                        ? employeeRepository.findById(dto.recipientEmployeeId()).orElse(null)
                        : null)
                .recipientTeam(
                        dto.recipientTeamId() != null ? teamRepository.findById(dto.recipientTeamId()).orElse(null)
                                : null)
                .message(dto.message()).isComment(true).anonymous(dto.anonymous()).senderEmployeeName(sender.getName()) // stored
                                                                                                                        // internally
                .senderName(dto.anonymous() ? "Anonymous" : sender.getName()) // display name
                .createdAt(LocalDateTime.now()).build();

        kudosRepository.save(kudos);

        return ResponseEntity.ok(Map.of("message", "Comment sent successfully"));
    }

    // ---------------- EMPLOYEE HISTORY ----------------
    @Operation(summary = "Get kudos and comments received by an employee")
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<?> getEmployeeHistory(@PathVariable Long employeeId,
            @RequestParam(defaultValue = "all") String period) {
        return ResponseEntity.ok(kudosService.getKudosHistoryForEmployee(employeeId, period));
    }

    // ---------------- TEAM HISTORY ----------------
    @Operation(summary = "Get kudos and comments received by a team")
    @GetMapping("/team/{teamId}/history")
    public ResponseEntity<?> getTeamHistory(@PathVariable Long teamId,
            @RequestParam(defaultValue = "all") String period) {
        return ResponseEntity.ok(kudosService.getKudosHistoryForTeam(teamId, period));
    }

    // ---------------- PUBLIC SEARCH: SYSTEM-WIDE RECENT ----------------
    @Operation(summary = "Public search - Recent system-wide kudos/comments", description = "Lists the 10 most recent kudos or comments across all employees and teams.", tags = {
            "Public Search" })
    @GetMapping("/public/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentKudos() {
        List<Map<String, Object>> recent = kudosService.getRecentKudos();
        return ResponseEntity.ok(recent);
    }

    // ---------------- LEADERBOARDS: TOP 5 EMPLOYEES ----------------
    @Operation(summary = "Top 5 Employees by Kudos (Past Month)", description = "Returns the top 5 employees by kudos received in the past month. Optional filter by department.")
    @GetMapping("/leaderboard/employees")
    public ResponseEntity<List<Map<String, Object>>> getTopEmployees(
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(kudosService.getTopEmployees(department));
    }

    // ---------------- LEADERBOARDS: TOP 5 TEAMS ----------------
    @Operation(summary = "Top 5 Teams by Kudos (Past Month)", description = "Returns the top 5 teams by kudos received in the past month. Optional filter by department or team name.")
    @GetMapping("/leaderboard/teams")
    public ResponseEntity<List<Map<String, Object>>> getTopTeams(@RequestParam(required = false) String department) {
        return ResponseEntity.ok(kudosService.getTopTeams(department));
    }

}
