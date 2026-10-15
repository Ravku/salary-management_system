package com.acme.hr.dto;

import java.time.LocalDate;

public record EmployeeRequest(
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String country,
        String department,
        String jobTitle,
        LocalDate hireDate,
        boolean active
) {
}
