package com.hrmtracker.repository;

import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    long countByRoleName(String roleName);

    @Query("SELECT u FROM User u JOIN FETCH u.department d JOIN FETCH u.role r WHERE r.name = :roleName")
    List<User> findAllByRoleNameWithDepartment(String roleName);

    List<User> findByRole(Role role);
}
