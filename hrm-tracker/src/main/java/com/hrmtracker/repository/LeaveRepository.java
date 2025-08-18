package com.hrmtracker.repository;

import com.hrmtracker.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    long countByStatus(String status);
}
