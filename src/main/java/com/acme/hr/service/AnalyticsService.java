package com.acme.hr.service;

import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.GroupAnalytics;
import com.acme.hr.dto.SalaryStatistics;
import com.acme.hr.config.CacheConfig;
import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import com.acme.hr.repository.EmployeeRepository;
import com.acme.hr.repository.SalaryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;

    public AnalyticsService(EmployeeRepository employeeRepository, SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.ANALYTICS_SUMMARY)
    public AnalyticsSummary summary() {
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, EmployeeSalary> latestSalaries = latestSalaries();
        return new AnalyticsSummary(
                employees.size(),
                employees.stream().filter(Employee::isActive).count(),
                salaryStatistics(employees.stream()
                        .map(employee -> latestSalaries.get(employee.getId()))
                        .filter(java.util.Objects::nonNull)
                        .toList())
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.ANALYTICS_BY_COUNTRY)
    public List<GroupAnalytics> byCountry() {
        return grouped(Employee::getCountry);
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.ANALYTICS_BY_DEPARTMENT)
    public List<GroupAnalytics> byDepartment() {
        return grouped(Employee::getDepartment);
    }

    private List<GroupAnalytics> grouped(Function<Employee, String> classifier) {
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, EmployeeSalary> latestSalaries = latestSalaries();
        Map<String, List<Employee>> groups = employees.stream().collect(Collectors.groupingBy(
                employee -> classifier.apply(employee) == null ? "Unspecified" : classifier.apply(employee),
                TreeMap::new, Collectors.toList()));
        return groups.entrySet().stream().map(entry -> {
            List<Employee> groupEmployees = entry.getValue();
            List<EmployeeSalary> salaries = groupEmployees.stream()
                    .map(employee -> latestSalaries.get(employee.getId()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return new GroupAnalytics(
                    entry.getKey(),
                    groupEmployees.size(),
                    groupEmployees.stream().filter(Employee::isActive).count(),
                    salaryStatistics(salaries));
        }).toList();
    }

    private Map<Long, EmployeeSalary> latestSalaries() {
        Map<Long, EmployeeSalary> latest = new LinkedHashMap<>();
        salaryRepository.findAllByOrderByEffectiveDateDesc().stream()
                .sorted(Comparator.comparing(EmployeeSalary::getEffectiveDate).reversed())
                .forEach(salary -> latest.putIfAbsent(salary.getEmployee().getId(), salary));
        return latest;
    }

    private List<SalaryStatistics> salaryStatistics(List<EmployeeSalary> salaries) {
        Map<String, List<EmployeeSalary>> byCurrency = salaries.stream().collect(Collectors.groupingBy(
                EmployeeSalary::getCurrency, TreeMap::new, Collectors.toList()));
        return byCurrency.entrySet().stream().map(entry -> {
            double total = entry.getValue().stream().mapToDouble(EmployeeSalary::getBaseSalary).sum();
            return new SalaryStatistics(
                    entry.getKey(), entry.getValue().size(), total / entry.getValue().size(), total);
        }).toList();
    }
}
