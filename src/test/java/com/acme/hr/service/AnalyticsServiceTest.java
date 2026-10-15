package com.acme.hr.service;

import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.GroupAnalytics;
import com.acme.hr.dto.SalaryStatistics;
import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import com.acme.hr.repository.EmployeeRepository;
import com.acme.hr.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SalaryRepository salaryRepository;

    private AnalyticsService analyticsService;
    private Employee a;
    private Employee b;
    private Employee c;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(employeeRepository, salaryRepository);
        a = employee(1L, "India", "Engineering", true);
        b = employee(2L, "India", "Finance", false);
        c = employee(3L, "USA", "Engineering", true);
        lenient().when(employeeRepository.findAll()).thenReturn(List.of(a, b, c));
        lenient().when(salaryRepository.findAllByOrderByEffectiveDateDesc()).thenReturn(List.of(
                salary(a, 110000.0, "INR", LocalDate.of(2026, 1, 1)),
                salary(b, 90000.0, "INR", LocalDate.of(2026, 1, 1)),
                salary(a, 80000.0, "INR", LocalDate.of(2025, 1, 1)),
                salary(c, 80000.0, "USD", LocalDate.of(2026, 1, 1))
        ));
    }

    @Test
    void summaryCountsHeadcountAndCalculatesLatestSalaryPerCurrency() {
        AnalyticsSummary summary = analyticsService.summary();

        assertEquals(3, summary.totalEmployees());
        assertEquals(2, summary.activeEmployees());
        assertEquals(2, summary.salaryStatistics().size());
        SalaryStatistics inr = summary.salaryStatistics().getFirst();
        assertEquals("INR", inr.currency());
        assertEquals(2, inr.employeeCount());
        assertEquals(100000.0, inr.averageSalary());
        assertEquals(200000.0, inr.totalSalary());
        assertEquals("USD", summary.salaryStatistics().get(1).currency());
    }

    @Test
    void summaryHandlesNoEmployeesAndNoSalaries() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(salaryRepository.findAllByOrderByEffectiveDateDesc()).thenReturn(List.of());

        AnalyticsSummary summary = analyticsService.summary();

        assertEquals(0, summary.totalEmployees());
        assertEquals(0, summary.activeEmployees());
        assertTrue(summary.salaryStatistics().isEmpty());
    }

    @Test
    void byCountryGroupsHeadcountAndSalaryStatistics() {
        List<GroupAnalytics> results = analyticsService.byCountry();

        assertEquals(2, results.size());
        GroupAnalytics india = results.getFirst();
        assertEquals("India", india.group());
        assertEquals(2, india.employeeCount());
        assertEquals(1, india.activeEmployees());
        assertEquals(1, india.salaryStatistics().size());
        assertEquals(200000.0, india.salaryStatistics().getFirst().totalSalary());
    }

    @Test
    void byDepartmentGroupsHeadcountAndSalaryStatistics() {
        List<GroupAnalytics> results = analyticsService.byDepartment();

        assertEquals(2, results.size());
        GroupAnalytics engineering = results.getFirst();
        assertEquals("Engineering", engineering.group());
        assertEquals(2, engineering.employeeCount());
        assertEquals(2, engineering.activeEmployees());
        assertEquals(2, engineering.salaryStatistics().size());
    }

    @Test
    void groupedAnalyticsUsesUnspecifiedForMissingGroupAndEmptyStatisticsWhenNoSalary() {
        Employee withoutSalary = employee(4L, null, null, false);
        when(employeeRepository.findAll()).thenReturn(List.of(withoutSalary));
        when(salaryRepository.findAllByOrderByEffectiveDateDesc()).thenReturn(List.of());

        GroupAnalytics result = analyticsService.byCountry().getFirst();

        assertEquals("Unspecified", result.group());
        assertEquals(1, result.employeeCount());
        assertEquals(0, result.activeEmployees());
        assertTrue(result.salaryStatistics().isEmpty());
    }

    private static Employee employee(Long id, String country, String department, boolean active) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setCountry(country);
        employee.setDepartment(department);
        employee.setActive(active);
        return employee;
    }

    private static EmployeeSalary salary(Employee employee, double amount, String currency, LocalDate date) {
        EmployeeSalary salary = new EmployeeSalary();
        salary.setEmployee(employee);
        salary.setBaseSalary(amount);
        salary.setCurrency(currency);
        salary.setEffectiveDate(date);
        return salary;
    }
}
