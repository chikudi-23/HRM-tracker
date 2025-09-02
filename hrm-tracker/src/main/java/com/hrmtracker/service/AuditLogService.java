package com.hrmtracker.service;

import com.hrmtracker.entity.AuditLog;
import com.hrmtracker.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String action, String entity, String oldValue, String newValue) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String performedBy = (authentication != null) ? authentication.getName() : "SYSTEM";

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setUsername(performedBy);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
