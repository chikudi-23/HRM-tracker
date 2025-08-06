package com.hrmtracker.config;

import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.Role;
import com.hrmtracker.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;


    @Override
    public void run(String... args) {

        // Add default departments if not exists
        if (departmentRepository.count() == 0) {
            departmentRepository.save(Department.builder().name("Human Resources").description("HR Department").build());
            departmentRepository.save(Department.builder().name("IT").description("IT Department").build());
        }

    }
}
