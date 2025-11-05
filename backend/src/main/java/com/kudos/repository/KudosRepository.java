package com.kudos.repository;

import com.kudos.model.Kudos;
import com.kudos.model.Employee;
import com.kudos.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KudosRepository extends JpaRepository<Kudos, Long> {

    // Recent kudos system-wide
    List<Kudos> findTop10ByOrderByCreatedAtDesc();

    // Kudos for specific employee or team
    List<Kudos> findByRecipientEmployee(Employee employee);

    List<Kudos> findByRecipientTeam(Team team);

    // Kudos given within a time window (e.g., last month)
    List<Kudos> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Fetch all Kudos sent by a particular sender (for analytics or moderation)
    List<Kudos> findBySenderEmployeeName(String senderEmployeeName);

    // For spam prevention: kudos sent by sender within a specific time range
    List<Kudos> findBySenderEmployeeNameAndCreatedAtBetween(
            String senderEmployeeName,
            LocalDateTime start,
            LocalDateTime end);

    // For searching/filtering
    List<Kudos> findByRecipientEmployeeAndCreatedAtBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    List<Kudos> findByRecipientTeamAndCreatedAtBetween(Team team, LocalDateTime start, LocalDateTime end);
}
