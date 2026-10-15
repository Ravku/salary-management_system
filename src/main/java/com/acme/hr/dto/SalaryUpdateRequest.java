package com.acme.hr.dto;

import java.time.LocalDate;

public record SalaryUpdateRequest(
        Double baseSalary,
        String currency,
        LocalDate effectiveDate
) {
}
