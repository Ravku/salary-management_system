package com.acme.hr.dto;

import java.time.LocalDate;

public record EmployeeSalaryResponse(
        Long id,
        Double baseSalary,
        String currency,
        LocalDate effectiveDate
) {
}
