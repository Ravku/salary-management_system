package com.acme.hr.controller;

import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.EmployeeRequest;
import com.acme.hr.dto.SalaryUpdateRequest;
import com.acme.hr.service.EmployeeSearchCacheService;
import com.acme.hr.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeSearchCacheService employeeSearchCacheService;

    public EmployeeController(
            EmployeeService employeeService, EmployeeSearchCacheService employeeSearchCacheService) {
        this.employeeService = employeeService;
        this.employeeSearchCacheService = employeeSearchCacheService;
    }

    @GetMapping
    public Page<EmployeeDetailsResponse> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return employeeSearchCacheService.findEmployees(search, country, department, jobTitle, active, pageable)
                .toPage();
    }

    @GetMapping("/{id}")
    public EmployeeDetailsResponse getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDetailsResponse createEmployee(@RequestBody EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    public EmployeeDetailsResponse updateEmployee(
            @PathVariable Long id, @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @PutMapping("/{id}/salary")
    public EmployeeDetailsResponse updateSalary(
            @PathVariable Long id, @RequestBody SalaryUpdateRequest request) {
        return employeeService.updateSalary(id, request);
    }
}
