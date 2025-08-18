package com.hrmtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class EmployeeDTO {

    private String name;
    private String email;
    private String phone;
    private String departmentName;
    private String authProvider;
    private String idProofPath;   // added field
    private String resumePath;    // added field

    // Constructor with all fields
    public EmployeeDTO(String name, String email, String phone,
                       String departmentName, String authProvider,
                       String idProofPath, String resumePath) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.departmentName = departmentName;
        this.authProvider = authProvider;
        this.idProofPath = idProofPath;
        this.resumePath = resumePath;
    }

    // Getters and setters for all fields (can be generated via Lombok or your IDE)
    // ...
}
