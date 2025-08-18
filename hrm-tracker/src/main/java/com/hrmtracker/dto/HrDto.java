package com.hrmtracker.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HrDto {
    private String name;
    private String email;
    private String phone;
    private String departmentName;
    private String roleName;
    private String idProofPath;
    private String resumePath;
}
