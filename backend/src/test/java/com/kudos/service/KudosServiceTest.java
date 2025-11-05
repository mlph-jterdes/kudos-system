package com.kudos.service;

import com.kudos.model.Employee;
import com.kudos.model.Kudos;
import com.kudos.model.Team;
import com.kudos.repository.EmployeeRepository;
import com.kudos.repository.KudosRepository;
import com.kudos.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KudosServiceTest {

    @Mock
    private KudosRepository kudosRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private KudosService kudosService;

    private Employee sender;
    private Employee recipient;
    private Team team;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = new Employee();
        sender.setId(1L);
        sender.setEmployeeId("E001");
        sender.setName("John");

        recipient = new Employee();
        recipient.setId(2L);
        recipient.setEmployeeId("E002");
        recipient.setName("Jane");
        recipient.setKudosCount(0);

        team = new Team();
        team.setId(1L);
        team.setName("Dev Team");
        team.setKudosCount(0);
    }

    // ---------- SEND KUDOS TO EMPLOYEE ----------

    @Test
    void testSendKudosToEmployee() {
        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(kudosRepository.findBySenderEmployeeName("E001")).thenReturn(List.of());
        when(kudosRepository.save(any(Kudos.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Kudos kudos = kudosService.sendKudos("E001", "Great work!", 2L, null, false);

        assertNotNull(kudos);
        assertEquals("John", kudos.getSenderName());
        assertEquals("E001", kudos.getSenderEmployeeName());
        assertEquals(recipient, kudos.getRecipientEmployee());
        assertEquals("Great work!", kudos.getMessage());
        assertFalse(kudos.isAnonymous());
        assertFalse(kudos.isComment());

        assertEquals(1, recipient.getKudosCount());
    }

    // ---------- SEND COMMENT TO EMPLOYEE ----------

    @Test
    void testSendCommentToEmployee() {
        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(kudosRepository.save(any(Kudos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Kudos comment = kudosService.sendComment("E001", "Nice job!", 2L, null, true);

        assertNotNull(comment);
        assertEquals("Anonymous", comment.isAnonymous() ? "Anonymous" : comment.getSenderName());
        assertTrue(comment.isComment());
        assertEquals(recipient, comment.getRecipientEmployee());
    }

    // ---------- DUPLICATE KUDOS ----------

    @Test
    void testSendDuplicateKudosThrows() {
        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(recipient));

        Kudos existing = new Kudos();
        existing.setRecipientEmployee(recipient);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setComment(false);

        when(kudosRepository.findBySenderEmployeeName("E001")).thenReturn(List.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> kudosService.sendKudos("E001", "Again!", 2L, null, false));

        assertEquals("You’ve already sent kudos to this recipient today!", exception.getMessage());
    }

    // ---------- GET RECENT KUDOS ----------

    @Test
    void testGetRecentKudos() {
        Kudos k = new Kudos();
        k.setId(1L);
        k.setSenderName("John");
        k.setMessage("Good job");
        k.setRecipientEmployee(recipient);
        k.setAnonymous(false);
        k.setComment(false);
        k.setCreatedAt(LocalDateTime.now());

        when(kudosRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(k));

        var recent = kudosService.getRecentKudos();
        assertEquals(1, recent.size());
        assertEquals("John", recent.get(0).get("sender"));
        assertEquals("Jane", recent.get(0).get("recipient"));
    }
}
