package com.acme.hr.dto;

import java.util.List;

public record GroupAnalytics(
        String group,
        long employeeCount,
        long activeEmployees,
        List<SalaryStatistics> salaryStatistics
) {
}
