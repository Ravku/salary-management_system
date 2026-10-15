package com.acme.hr.repository;

import com.acme.hr.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    List<EmployeeSalary> findAllByEmployee_IdOrderByEffectiveDateDesc(Long employeeId);
    List<EmployeeSalary> findAllByOrderByEffectiveDateDesc();
}