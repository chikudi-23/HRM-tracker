package com.hrmtracker.config;

import com.hrmtracker.entity.Announcement;
import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.AnnouncementRepository;
import com.hrmtracker.repository.DepartmentRepository;
import com.hrmtracker.repository.RoleRepository;
import com.hrmtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Override
    public void run(String... args) {

        // ✅ Add default departments if not exists
        if (departmentRepository.count() == 0) {
            departmentRepository.save(Department.builder().name("Human Resources").description("HR Department").build());
            departmentRepository.save(Department.builder().name("IT").description("IT Department").build());
            departmentRepository.save(Department.builder().name("Finance").description("Finance Department").build());

        }

        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name("ADMIN").build());
            roleRepository.save(Role.builder().name("HR").build());
            roleRepository.save(Role.builder().name("EMPLOYEE").build());
        }

        // ✅ Add default announcements if not exists
        if (announcementRepository.count() == 0) {
            User adminUser = userRepository.findByEmail("sakshi@gmail.com").orElse(null);
            if (adminUser != null) {
                announcementRepository.save(
                        Announcement.builder()
                                .title("System Maintenance")
                                .content("System will be down tonight from 11 PM to 1 AM.")
                                .createdAt(LocalDateTime.now())
                                .createdBy(adminUser)
                                .build()
                );

                announcementRepository.save(
                        Announcement.builder()
                                .title("New HR Policies")
                                .content("Please review the updated HR policies effective from next month.")
                                .createdAt(LocalDateTime.now())
                                .createdBy(adminUser)
                                .build()
                );
            }
        }
    }
}
