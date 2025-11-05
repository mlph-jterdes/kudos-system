package com.kudos.service;

import com.kudos.model.Employee;
import com.kudos.model.Kudos;
import com.kudos.model.Team;
import com.kudos.repository.EmployeeRepository;
import com.kudos.repository.KudosRepository;
import com.kudos.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KudosService {

    private final KudosRepository kudosRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    // ---------------- PUBLIC SEARCH: HISTORY ----------------
    public Map<String, Object> getPublicHistory(Long employeeId, Long teamId, String period, boolean showAllMessages) {
        Map<String, Object> result = new HashMap<>();
        List<Kudos> kudosList;

        LocalDateTime startDate;
        switch (period.toLowerCase()) {
        case "week" -> startDate = LocalDateTime.now().minusWeeks(1);
        case "month" -> startDate = LocalDateTime.now().minusMonths(1);
        default -> startDate = null;
        }

        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            kudosList = employee.getReceivedKudos().stream()
                    .filter(k -> startDate == null || k.getCreatedAt().isAfter(startDate))
                    .sorted(Comparator.comparing(Kudos::getCreatedAt).reversed()).collect(Collectors.toList());

            result.put("type", "employee");
            result.put("name", employee.getName());
            result.put("kudosCount", employee.getKudosCount());
            result.put("messages", showAllMessages ? kudosList.stream().map(this::toMessageDTO).toList()
                    : kudosList.stream().limit(5).map(this::toMessageDTO).toList());

        } else if (teamId != null) {
            Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));

            kudosList = team.getMembers().stream().flatMap(emp -> emp.getReceivedKudos().stream())
                    .filter(k -> startDate == null || k.getCreatedAt().isAfter(startDate))
                    .sorted(Comparator.comparing(Kudos::getCreatedAt).reversed()).collect(Collectors.toList());

            int totalKudos = team.getMembers().stream().mapToInt(Employee::getKudosCount).sum();

            result.put("type", "team");
            result.put("name", team.getName());
            result.put("kudosCount", totalKudos);
            result.put("messages", showAllMessages ? kudosList.stream().map(this::toMessageDTO).toList()
                    : kudosList.stream().limit(5).map(this::toMessageDTO).toList());
        }

        return result;
    }

    // ---------------- SEND KUDOS ----------------
    public Kudos sendKudos(String senderEmployeeId, String message, Long recipientEmployeeId, Long recipientTeamId,
            boolean anonymous) {

        // Lookup sender
        Employee sender = employeeRepository.findByEmployeeId(senderEmployeeId)
                .orElseThrow(() -> new RuntimeException("Sender not found."));

        // Recipient checks
        final Employee recipientEmployee;
        final Team recipientTeam;

        if (recipientEmployeeId != null) {
            recipientEmployee = employeeRepository.findById(recipientEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Recipient employee not found."));
            recipientTeam = null;
        } else if (recipientTeamId != null) {
            recipientTeam = teamRepository.findById(recipientTeamId)
                    .orElseThrow(() -> new RuntimeException("Recipient team not found."));
            recipientEmployee = null;
        } else {
            throw new RuntimeException("Recipient required (employee or team)");
        }

        // Prevent duplicate kudos in a single day
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Fetch all kudos sent by this employee today
        List<Kudos> todaysKudos = kudosRepository.findBySenderEmployeeName(senderEmployeeId);
        boolean duplicate = todaysKudos.stream().anyMatch(k -> !k.isComment() && // ignore comments
                Objects.equals(k.getRecipientEmployee(), recipientEmployee) && // same employee
                k.getCreatedAt().isAfter(startOfDay) && k.getCreatedAt().isBefore(endOfDay));

        if (duplicate) {
            throw new RuntimeException("You’ve already sent kudos to this recipient today!");
        }

        // Create kudos
        Kudos kudos = new Kudos();
        kudos.setSenderName(sender.getName());
        kudos.setSenderEmployeeName(senderEmployeeId);
        kudos.setRecipientEmployee(recipientEmployee);
        kudos.setRecipientTeam(recipientTeam);
        kudos.setMessage(message);
        kudos.setAnonymous(anonymous);
        kudos.setCreatedAt(LocalDateTime.now());
        kudos.setComment(false);

        // Update kudos count (for leaderboard)
        if (recipientEmployee != null) {
            recipientEmployee.setKudosCount(recipientEmployee.getKudosCount() + 1);
            employeeRepository.save(recipientEmployee);
        }

        if (recipientTeam != null) {
            recipientTeam.setKudosCount(recipientTeam.getKudosCount() + 1);
            teamRepository.save(recipientTeam);
        }

        // Save
        return kudosRepository.save(kudos);
    }

    // ---------------- SEND COMMENT ----------------
    public Kudos sendComment(String senderEmployeeId, String message, Long recipientEmployeeId, Long recipientTeamId,
            boolean anonymous) {

        // Lookup sender
        Employee sender = employeeRepository.findByEmployeeId(senderEmployeeId)
                .orElseThrow(() -> new RuntimeException("Sender not found."));

        // Recipients
        final Employee recipientEmployee;
        final Team recipientTeam;

        if (recipientEmployeeId != null) {
            recipientEmployee = employeeRepository.findById(recipientEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Recipient employee not found."));
            recipientTeam = null;
        } else if (recipientTeamId != null) {
            recipientTeam = teamRepository.findById(recipientTeamId)
                    .orElseThrow(() -> new RuntimeException("Recipient team not found."));
            recipientEmployee = null;
        } else {
            throw new RuntimeException("Recipient required (employee or team)");
        }

        // Create comment Kudos
        Kudos comment = new Kudos();
        comment.setSenderName(sender.getName());
        comment.setSenderEmployeeName(senderEmployeeId);
        comment.setRecipientEmployee(recipientEmployee);
        comment.setRecipientTeam(recipientTeam);
        comment.setMessage(message);
        comment.setAnonymous(anonymous);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setComment(true);

        return kudosRepository.save(comment);
    }

    // ---------------- RECENT KUDOS ----------------
    public List<Map<String, Object>> getRecentKudos() {
        return kudosRepository.findTop10ByOrderByCreatedAtDesc().stream().map(k -> {
            String recipientName = "N/A";
            if (k.getRecipientEmployee() != null) {
                recipientName = k.getRecipientEmployee().getName();
            } else if (k.getRecipientTeam() != null) {
                recipientName = k.getRecipientTeam().getName();
            }

            String senderDisplay = k.isAnonymous() ? "Anonymous" : k.getSenderName();
            String type = k.isComment() ? "Comment" : "Kudos";

            Map<String, Object> map = new HashMap<>();
            map.put("id", k.getId());
            map.put("type", type);
            map.put("sender", senderDisplay);
            map.put("message", k.getMessage());
            map.put("recipient", recipientName);
            map.put("createdAt", k.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    // ---------------- GET EMPLOYEE KUDOS HISTORY ----------------
    public List<Map<String, Object>> getEmployeeKudosHistory(Long employeeId, String period) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found."));

        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now();

        List<Kudos> kudosList = kudosRepository.findByRecipientEmployeeAndCreatedAtBetween(emp, start, end);

        return mapKudosList(kudosList);
    }

    // ---------------- GET TEAM KUDOS HISTORY ----------------
    public List<Map<String, Object>> getTeamKudosHistory(Long teamId, String period) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found."));

        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now();

        List<Kudos> kudosList = kudosRepository.findByRecipientTeamAndCreatedAtBetween(team, start, end);

        return mapKudosList(kudosList);
    }

    // ---------------- HISTORY: EMPLOYEE ----------------
    public Map<String, Object> getKudosHistoryForEmployee(Long employeeId, String period) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Kudos> allKudos = kudosRepository.findByRecipientEmployee(employee);

        // Filter by period
        LocalDateTime cutoff = getCutoff(period);
        if (cutoff != null) {
            allKudos = allKudos.stream().filter(k -> k.getCreatedAt().isAfter(cutoff)).collect(Collectors.toList());
        }

        // Sort newest first
        List<Map<String, Object>> messages = allKudos.stream()
                .sorted(Comparator.comparing(Kudos::getCreatedAt).reversed()).map(this::mapSingleKudos)
                .collect(Collectors.toList());

        long totalKudos = allKudos.stream().filter(k -> !k.isComment()).count();

        return Map.of("type", "Employee", "name", employee.getName(), "totalKudos", totalKudos, "period", period,
                "messages", messages);
    }

    // ---------------- HISTORY: TEAM ----------------
    public Map<String, Object> getKudosHistoryForTeam(Long teamId, String period) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));

        List<Kudos> allKudos = kudosRepository.findByRecipientTeam(team);

        // Filter by period
        LocalDateTime cutoff = getCutoff(period);
        if (cutoff != null) {
            allKudos = allKudos.stream().filter(k -> k.getCreatedAt().isAfter(cutoff)).collect(Collectors.toList());
        }

        // Sort newest first
        List<Map<String, Object>> messages = allKudos.stream()
                .sorted(Comparator.comparing(Kudos::getCreatedAt).reversed()).map(this::mapSingleKudos)
                .collect(Collectors.toList());

        long totalKudos = allKudos.stream().filter(k -> !k.isComment()).count();

        return Map.of("type", "Team", "name", team.getName(), "totalKudos", totalKudos, "period", period, "messages",
                messages);
    }

    // ---------------- LEADERBOARD: TOP 5 EMPLOYEES ----------------
    public List<Map<String, Object>> getTopEmployees(String department) {
        // Fetch all employees
        List<Employee> employees = employeeRepository.findAll();

        // Filter by department if provided
        if (department != null && !department.isBlank()) {
            employees = employees.stream()
                    .filter(e -> e.getDepartment() != null && department.equalsIgnoreCase(e.getDepartment()))
                    .collect(Collectors.toList());
        }

        // Sort by kudosCount descending and take top 5
        return employees.stream().filter(e -> e.getKudosCount() > 0) // remove teams with 0 total kudos
                .sorted(Comparator.comparingInt(Employee::getKudosCount).reversed()).limit(5).map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("employee", e.getName());
                    map.put("department", e.getDepartment());
                    map.put("kudosCount", e.getKudosCount());
                    return map;
                }).collect(Collectors.toList());
    }

    // ---------------- LEADERBOARD: TOP 5 TEAMS ----------------
    public List<Map<String, Object>> getTopTeams(String department) {
        // Fetch all teams
        List<Team> teams = teamRepository.findAll();

        // If department filtering is needed, only include teams with members in that
        // department
        if (department != null && !department.isBlank()) {
            teams = teams.stream()
                    .filter(team -> team.getMembers() != null && team.getMembers().stream()
                            .anyMatch(emp -> department.equalsIgnoreCase(emp.getDepartment())))
                    .collect(Collectors.toList());
        }

        // Map each team to its total kudos count (sum of members' kudosCount)
        Map<Team, Integer> teamKudos = teams.stream()
                .collect(Collectors.toMap(team -> team, team -> team.getMembers() == null ? 0
                        : team.getMembers().stream().mapToInt(Employee::getKudosCount).sum()));

        // Sort by total kudos descending and take top 5
        return teamKudos.entrySet().stream().filter(entry -> entry.getValue() > 0) // remove teams with 0 total kudos
                .sorted(Map.Entry.<Team, Integer>comparingByValue().reversed()).limit(5).map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("team", entry.getKey().getName());
                    map.put("kudosCount", entry.getValue());
                    return map;
                }).collect(Collectors.toList());
    }

    // ---------- HELPERS ----------
    private LocalDateTime getStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
        case "week" -> now.minusWeeks(1);
        case "month" -> now.minusMonths(1);
        default -> now.minusYears(10); // default: show everything
        };
    }

    private List<Map<String, Object>> mapKudosList(List<Kudos> kudosList) {
        return kudosList.stream().map(this::mapSingleKudos).collect(Collectors.toList());
    }

    private Map<String, Object> mapSingleKudos(Kudos k) {
        String senderDisplay = k.isAnonymous() ? "Anonymous" : k.getSenderName();
        String recipientName = k.getRecipientEmployee() != null ? k.getRecipientEmployee().getName()
                : (k.getRecipientTeam() != null ? k.getRecipientTeam().getName() : "N/A");
        return Map.of("id", k.getId(), "sender", senderDisplay, "message", k.getMessage(), "recipient", recipientName,
                "type", k.isComment() ? "Comment" : "Kudos", "createdAt", k.getCreatedAt());
    }

    private LocalDateTime getCutoff(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
        case "week":
            return now.minusWeeks(1);
        case "month":
            return now.minusMonths(1);
        case "all":
            return null; // No cutoff — return everything
        default:
            throw new IllegalArgumentException("Invalid period. Use 'week', 'month', or 'all'.");
        }
    }

    // map Kudos entity to DTO for JSON
    private Map<String, Object> toMessageDTO(Kudos k) {
        return Map.of("id", k.getId(), "sender", k.getSenderName() != null ? k.getSenderName() : "System", "isComment",
                k.isComment(), "message", k.getMessage(), "createdAt", k.getCreatedAt());
    }
}
