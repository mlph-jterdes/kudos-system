package com.kudos.repository;

import com.kudos.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    // Search by name (for public search)
    List<Employee> findByNameContainingIgnoreCase(String name);

    // Leaderboard - top employees by kudos count
    List<Employee> findTop5ByOrderByKudosCountDesc();

    // Leaderboard filtered by department
    List<Employee> findTop5ByDepartmentOrderByKudosCountDesc(String department);

    // Employees by department
    List<Employee> findByDepartmentIgnoreCase(String department);
}
