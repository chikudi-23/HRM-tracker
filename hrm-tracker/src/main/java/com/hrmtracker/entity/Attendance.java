package com.hrmtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private LocalTime checkInTime;   // Renamed from checkIn
    private LocalTime checkOutTime;  // Renamed from checkOut
    private Long totalHours;         // Hours worked

    private String status;           // Present / Absent / Leave

    private Boolean leave1;           // Leave flag: true if on leave

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
