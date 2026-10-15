package com.acme.hr.dto;

import java.util.List;

public record AnalyticsSummary(
        long totalEmployees,
        long activeEmployees,
        List<SalaryStatistics> salaryStatistics
) {
}
