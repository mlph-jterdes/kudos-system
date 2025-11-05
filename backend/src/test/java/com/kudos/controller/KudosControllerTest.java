//package com.kudos.controller;
//
//import com.kudos.dto.KudosDTO;
//import com.kudos.model.Employee;
//import com.kudos.model.Kudos;
//import com.kudos.repository.EmployeeRepository;
//import com.kudos.repository.KudosRepository;
//import com.kudos.repository.TeamRepository;
//import com.kudos.service.EmployeeService;
//import com.kudos.service.KudosService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class KudosControllerTest {
//
//    @Mock
//    private KudosService kudosService;
//
//    @Mock
//    private EmployeeService employeeService;
//
//    // ✅ Add missing repositories to avoid NullPointerException
//    @Mock
//    private EmployeeRepository employeeRepository;
//
//    @Mock
//    private TeamRepository teamRepository;
//
//    @Mock
//    private KudosRepository kudosRepository;
//
//    @InjectMocks
//    private KudosController kudosController;
//
//    private Employee mockSender;
//    private Kudos mockKudos;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        mockSender = new Employee();
//        mockSender.setEmployeeId("E001");
//        mockSender.setName("John Doe");
//
//        mockKudos = new Kudos();
//        mockKudos.setId(1L);
//        mockKudos.setMessage("Great job!");
//        mockKudos.setSenderName("John Doe");
//        mockKudos.setSenderEmployeeName("E001");
//        mockKudos.setCreatedAt(LocalDateTime.now());
//        mockKudos.setComment(false);
//        mockKudos.setAnonymous(false);
//    }
//
//    // ---------------- TEST SEND KUDOS ----------------
//    @Test
//    void testSendKudos_Success() {
//        KudosDTO dto = new KudosDTO("Great job!", 2L, null, false, false);
//
//        when(employeeService.findByEmail("john@example.com")).thenReturn(Optional.of(mockSender));
//        when(kudosService.sendKudos("E001", "Great job!", 2L, null, false)).thenReturn(mockKudos);
//
//        ResponseEntity<?> response = kudosController.sendKudos("john@example.com", dto);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        assertEquals("Kudos sent successfully!", body.get("message"));
//        assertEquals(mockKudos, body.get("data"));
//
//        verify(kudosService, times(1)).sendKudos("E001", "Great job!", 2L, null, false);
//    }
//
//    @Test
//    void testSendKudos_SenderNotFound() {
//        KudosDTO dto = new KudosDTO("Great job!", 2L, null, false, false);
//        when(employeeService.findByEmail("john@example.com")).thenReturn(Optional.empty());
//
//        ResponseEntity<?> response = kudosController.sendKudos("john@example.com", dto);
//
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        assertEquals("Sender not found", body.get("error"));
//    }
//
//    // ---------------- TEST SEND COMMENT ----------------
//    @Test
//    void testSendComment_Success() {
//        when(employeeService.findByEmail("john@example.com")).thenReturn(Optional.of(mockSender));
//
//        KudosDTO dto = new KudosDTO("Nice work!", 2L, null, false, true);
//
//        // ✅ Simulate employeeRepository lookup
//        when(employeeRepository.findById(2L)).thenReturn(Optional.of(new Employee()));
//
//        ResponseEntity<?> response = kudosController.sendComment("john@example.com", dto);
//
//        assertEquals(200, response.getStatusCodeValue());
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        assertEquals("Comment sent successfully", body.get("message"));
//
//        verify(employeeService, times(1)).findByEmail("john@example.com");
//    }
//
//    // ---------------- TEST GET RECENT KUDOS ----------------
//    @Test
//    void testGetRecentKudos() {
//        List<Map<String, Object>> recent = new ArrayList<>();
//        Map<String, Object> kudosMap = Map.of("id", 1L, "sender", "John Doe", "message", "Great job!", "recipient",
//                "Jane Smith", "type", "Kudos", "createdAt", LocalDateTime.now());
//        recent.add(kudosMap);
//
//        when(kudosService.getRecentKudos()).thenReturn(recent);
//
//        ResponseEntity<List<Map<String, Object>>> response = kudosController.getRecentKudos();
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(recent, response.getBody());
//        verify(kudosService, times(1)).getRecentKudos();
//    }
//
//    // ---------------- TEST GET TOP EMPLOYEES ----------------
//    @Test
//    void testGetTopEmployees() {
//        List<Map<String, Object>> topEmployees = List
//                .of(Map.of("employee", "John Doe", "department", "IT", "kudosCount", 5L));
//        when(kudosService.getTopEmployees("IT")).thenReturn(topEmployees);
//
//        ResponseEntity<List<Map<String, Object>>> response = kudosController.getTopEmployees("IT");
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(topEmployees, response.getBody());
//        verify(kudosService, times(1)).getTopEmployees("IT");
//    }
//
//    // ---------------- TEST GET TOP TEAMS ----------------
//    @Test
//    void testGetTopTeams() {
//        List<Map<String, Object>> topTeams = List.of(Map.of("team", "Engineering", "kudosCount", 8L));
//        when(kudosService.getTopTeams(null)).thenReturn(topTeams);
//
//        ResponseEntity<List<Map<String, Object>>> response = kudosController.getTopTeams(null);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(topTeams, response.getBody());
//        verify(kudosService, times(1)).getTopTeams(null);
//    }
//}
