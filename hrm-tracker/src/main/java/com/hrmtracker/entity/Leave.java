package com.hrmtracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
}
