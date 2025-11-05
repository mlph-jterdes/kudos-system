package com.kudos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "ADMIN", "MODERATOR", "EMPLOYEE"

    // JPA default constructor
    public Role() {
    }

    // Constructor
    public Role(String name) {
        this.name = name;
    }
}
