package com.acme.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "EMPLOYEE_SALARY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "employeeCode", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Double baseSalary;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDateTime updatedAt;
}
