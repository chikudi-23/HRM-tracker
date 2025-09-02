package com.hrmtracker.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
    private String timestamp;  // preformatted
    private String username;
    private String entity;
    private String action;
}
