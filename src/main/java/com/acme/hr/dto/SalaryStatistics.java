package com.acme.hr.dto;

public record SalaryStatistics(
        String currency,
        long employeeCount,
        double averageSalary,
        double totalSalary
) {
}
