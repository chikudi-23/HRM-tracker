package com.hrmtracker.service.impl;

import com.hrmtracker.entity.Department;
import com.hrmtracker.repository.DepartmentRepository;
import com.hrmtracker.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
