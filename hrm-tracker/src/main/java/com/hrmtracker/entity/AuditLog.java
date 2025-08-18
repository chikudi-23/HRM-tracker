package com.hrmtracker.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;       // e.g. CREATE, UPDATE, DELETE
    private String entity;       // e.g. Employee, Leave
    private String username;     // who performed the action
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    // Constructors
    public AuditLog() {}

    public AuditLog(String action, String entity, String username, String oldValue, String newValue) {
        this.action = action;
        this.entity = entity;
        this.username = username;
        this.timestamp = LocalDateTime.now();
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Getters and Setters
    // ... (generate using your IDE or manually if needed)
}
