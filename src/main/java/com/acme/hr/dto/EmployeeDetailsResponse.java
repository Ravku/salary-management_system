package com.acme.hr.dto;

import java.time.LocalDate;

public record EmployeeDetailsResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String country,
        String department,
        String jobTitle,
        LocalDate hireDate,
        boolean active,
        EmployeeSalaryResponse salary
) {
}
