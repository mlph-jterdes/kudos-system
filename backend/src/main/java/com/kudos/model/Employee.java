package com.kudos.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = { "employeeId", "email" }))
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeId; // From CSV upload

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String department; // e.g. "operations" or "support"

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "employee_teams", joinColumns = @JoinColumn(name = "employee_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams;

    @OneToMany(mappedBy = "recipientEmployee")
    private List<Kudos> receivedKudos;

    private int kudosCount = 0; // total kudos for leaderboard

    // (Optional: if you want timestamps)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
