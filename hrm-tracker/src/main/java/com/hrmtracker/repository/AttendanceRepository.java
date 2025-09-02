package com.hrmtracker.repository;

import com.hrmtracker.entity.Attendance;
import com.hrmtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUser(User user);
    Attendance findByUserAndDate(User user, LocalDate date);
}
