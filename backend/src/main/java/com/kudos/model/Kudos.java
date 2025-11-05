package com.kudos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kudos")
public class Kudos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- RECIPIENT ----------------
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee recipientEmployee; // may be null if sent to a team

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team recipientTeam; // may be null if sent to an employee

    // ---------------- SENDER ----------------
    private String senderName; // optional display name if not anonymous

    @Column(nullable = false)
    private String senderEmployeeName; // internal tracking (can be hidden if anonymous)

    private boolean anonymous = false;

    // ---------------- CONTENT ----------------
    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isComment = false; // true = comment only, false = kudos

    private LocalDateTime createdAt = LocalDateTime.now();
}
