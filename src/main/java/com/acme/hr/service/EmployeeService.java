package com.acme.hr.service;

import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.EmployeeRequest;
import com.acme.hr.dto.EmployeeSalaryResponse;
import com.acme.hr.dto.SalaryUpdateRequest;
import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import com.acme.hr.exception.ResourceNotFoundException;
import com.acme.hr.repository.EmployeeRepository;
import com.acme.hr.repository.SalaryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;

    public EmployeeService(EmployeeRepository employeeRepository, SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDetailsResponse> findEmployees(
            String search, String country, String department, String jobTitle, Boolean active, Pageable pageable) {
        Specification<Employee> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
                ));
            }
            addExactFilter(predicates, criteriaBuilder, root.get("country"), country);
            addExactFilter(predicates, criteriaBuilder, root.get("department"), department);
            addExactFilter(predicates, criteriaBuilder, root.get("jobTitle"), jobTitle);
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return employeeRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeDetailsResponse getEmployee(Long id) {
        return toResponse(getEmployeeEntity(id));
    }

    @Transactional
    public EmployeeDetailsResponse createEmployee(EmployeeRequest request) {
        validateEmployee(request);
        Employee employee = new Employee();
        applyRequest(employee, request);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDetailsResponse updateEmployee(Long id, EmployeeRequest request) {
        validateEmployee(request);
        Employee employee = getEmployeeEntity(id);
        applyRequest(employee, request);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDetailsResponse updateSalary(Long id, SalaryUpdateRequest request) {
        if (request == null || request.baseSalary() == null || request.baseSalary() < 0
                || request.currency() == null || request.currency().isBlank()
                || request.effectiveDate() == null) {
            throw new IllegalArgumentException("Salary must have a non-negative baseSalary, currency, and effectiveDate.");
        }
        Employee employee = getEmployeeEntity(id);
        List<EmployeeSalary> salaries = salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(id);
        EmployeeSalary salary = salaries.isEmpty() ? new EmployeeSalary() : salaries.getFirst();
        salary.setEmployee(employee);
        salary.setBaseSalary(request.baseSalary());
        salary.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        salary.setEffectiveDate(request.effectiveDate());
        salary.setUpdatedAt(LocalDateTime.now());
        salaryRepository.save(salary);
        return toResponse(employee);
    }

    private Employee getEmployeeEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee " + id + " was not found."));
    }

    private EmployeeDetailsResponse toResponse(Employee employee) {
        List<EmployeeSalary> salaries = salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(employee.getId());
        EmployeeSalaryResponse salary = salaries.isEmpty() ? null : toSalaryResponse(salaries.getFirst());
        return new EmployeeDetailsResponse(
                employee.getId(), employee.getEmployeeCode(), employee.getFirstName(), employee.getLastName(),
                employee.getEmail(), employee.getCountry(), employee.getDepartment(), employee.getJobTitle(),
                employee.getHireDate(), employee.isActive(), salary
        );
    }

    private EmployeeSalaryResponse toSalaryResponse(EmployeeSalary salary) {
        return new EmployeeSalaryResponse(
                salary.getId(), salary.getBaseSalary(), salary.getCurrency(), salary.getEffectiveDate());
    }

    private void applyRequest(Employee employee, EmployeeRequest request) {
        employee.setEmployeeCode(request.employeeCode().trim());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email().trim());
        employee.setCountry(request.country());
        employee.setDepartment(request.department());
        employee.setJobTitle(request.jobTitle());
        employee.setHireDate(request.hireDate());
        employee.setActive(request.active());
    }

    private void validateEmployee(EmployeeRequest request) {
        if (request == null || isBlank(request.employeeCode()) || isBlank(request.firstName()) || isBlank(request.email())) {
            throw new IllegalArgumentException("employeeCode, firstName, and email are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void addExactFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Path<String> path,
            String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(path), value.trim().toLowerCase(Locale.ROOT)));
        }
    }
}
