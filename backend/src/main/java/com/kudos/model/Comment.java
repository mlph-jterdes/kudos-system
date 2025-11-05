package com.kudos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee targetEmployee;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team targetTeam;

    private String senderName;
    private String senderEmployeeName; // actual name for tracking
    private boolean anonymous = false;

    @Column(nullable = false, length = 500)
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
