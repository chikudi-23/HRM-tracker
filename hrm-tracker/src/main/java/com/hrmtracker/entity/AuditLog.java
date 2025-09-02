package com.hrmtracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;       // e.g. CREATE, UPDATE, DELETE
    private String entity;       // e.g. Employee, Leave
    private String username;     // who performed the action

    @Column(name = "timestamp", columnDefinition = "DATETIME")
    private LocalDateTime timestamp;

    public AuditLog(String action, String entity, String username) {
        this.action = action;
        this.entity = entity;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }
}
