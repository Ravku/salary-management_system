package com.acme.hr.service;

import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.EmployeeRequest;
import com.acme.hr.dto.SalaryUpdateRequest;
import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import com.acme.hr.exception.ResourceNotFoundException;
import com.acme.hr.repository.EmployeeRepository;
import com.acme.hr.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SalaryRepository salaryRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, salaryRepository);
    }

    @Test
    void findEmployeesReturnsMappedPageAndAppliesAllFilters() {
        Employee employee = employee(1L, "EMP1", "Asha", "Patel", true);
        PageRequest pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(1L)).thenReturn(List.of());

        Page<com.acme.hr.dto.EmployeeDetailsResponse> result = employeeService.findEmployees(
                " asha ", "India", "Engineering", "Developer", true, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Asha", result.getContent().getFirst().firstName());
        assertNull(result.getContent().getFirst().salary());
        verify(employeeRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(), eq(pageable));
    }

    @Test
    void getEmployeeReturnsLatestSalary() {
        Employee employee = employee(7L, "EMP7", "Asha", "Patel", true);
        EmployeeSalary salary = salary(employee, 7L, 95000.0, "USD", LocalDate.of(2026, 1, 1));
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(7L)).thenReturn(List.of(salary));

        EmployeeDetailsResponse result = employeeService.getEmployee(7L);

        assertEquals(7L, result.id());
        assertNotNull(result.salary());
        assertEquals(95000.0, result.salary().baseSalary());
        assertEquals("USD", result.salary().currency());
    }

    @Test
    void getEmployeeThrowsWhenMissing() {
        when(employeeRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(55L));
        verifyNoInteractions(salaryRepository);
    }

    @Test
    void createEmployeeTrimsRequiredFieldsAndReturnsSavedEmployee() {
        EmployeeRequest request = request(" EMP8 ", " Asha ", " Patel ", " asha@example.com ");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(8L);
            return saved;
        });
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(8L)).thenReturn(List.of());

        EmployeeDetailsResponse result = employeeService.createEmployee(request);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("EMP8", captor.getValue().getEmployeeCode());
        assertEquals("Asha", captor.getValue().getFirstName());
        assertEquals("asha@example.com", captor.getValue().getEmail());
        assertEquals(8L, result.id());
    }

    @Test
    void createEmployeeRejectsNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
        verifyNoInteractions(employeeRepository, salaryRepository);
    }

    @Test
    void createEmployeeRejectsMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request(" ", "Asha", "Patel", "asha@example.com")));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request("EMP1", null, "Patel", "asha@example.com")));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request("EMP1", "Asha", "Patel", " ")));
        verifyNoInteractions(employeeRepository, salaryRepository);
    }

    @Test
    void updateEmployeeUpdatesExistingEmployee() {
        Employee employee = employee(3L, "OLD", "Old", "Name", true);
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(3L)).thenReturn(List.of());

        EmployeeDetailsResponse result = employeeService.updateEmployee(
                3L, request("EMP3", "New", "Person", "new@example.com"));

        assertEquals("EMP3", result.employeeCode());
        assertEquals("New", result.firstName());
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployeeThrowsWhenEmployeeMissing() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.updateEmployee(3L, request("EMP3", "New", "Person", "new@example.com")));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateSalaryCreatesSalaryWhenNoPreviousSalaryExists() {
        Employee employee = employee(4L, "EMP4", "Mia", "Lee", true);
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(4L)).thenReturn(List.of());
        when(salaryRepository.save(any(EmployeeSalary.class))).thenAnswer(invocation -> {
            EmployeeSalary salary = invocation.getArgument(0);
            salary.setId(41L);
            return salary;
        });

        EmployeeDetailsResponse result = employeeService.updateSalary(
                4L, new SalaryUpdateRequest(85000.0, " usd ", LocalDate.of(2026, 2, 1)));

        ArgumentCaptor<EmployeeSalary> captor = ArgumentCaptor.forClass(EmployeeSalary.class);
        verify(salaryRepository).save(captor.capture());
        assertSame(employee, captor.getValue().getEmployee());
        assertEquals(85000.0, captor.getValue().getBaseSalary());
        assertEquals("USD", captor.getValue().getCurrency());
        assertEquals(LocalDate.of(2026, 2, 1), captor.getValue().getEffectiveDate());
        assertEquals("EMP4", result.employeeCode());
    }

    @Test
    void updateSalaryUpdatesLatestSalaryRecord() {
        Employee employee = employee(5L, "EMP5", "Sam", "Wu", true);
        EmployeeSalary salary = salary(employee, 51L, 70000.0, "CAD", LocalDate.of(2025, 1, 1));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findAllByEmployee_IdOrderByEffectiveDateDesc(5L)).thenReturn(List.of(salary));
        when(salaryRepository.save(any(EmployeeSalary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.updateSalary(5L, new SalaryUpdateRequest(72000.0, "cad", LocalDate.of(2026, 1, 1)));

        verify(salaryRepository).save(salary);
        assertEquals(72000.0, salary.getBaseSalary());
        assertEquals("CAD", salary.getCurrency());
    }

    @Test
    void updateSalaryRejectsNullOrInvalidRequests() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateSalary(1L, null));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.updateSalary(1L, new SalaryUpdateRequest(null, "USD", LocalDate.now())));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.updateSalary(1L, new SalaryUpdateRequest(-1.0, "USD", LocalDate.now())));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.updateSalary(1L, new SalaryUpdateRequest(1.0, " ", LocalDate.now())));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.updateSalary(1L, new SalaryUpdateRequest(1.0, "USD", null)));
        verifyNoInteractions(employeeRepository, salaryRepository);
    }

    @Test
    void updateSalaryThrowsWhenEmployeeMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateSalary(
                99L, new SalaryUpdateRequest(50000.0, "USD", LocalDate.now())));
        verify(salaryRepository, never()).save(any());
    }

    private static EmployeeRequest request(String code, String firstName, String lastName, String email) {
        return new EmployeeRequest(code, firstName, lastName, email,
                "India", "Engineering", "Developer", LocalDate.of(2024, 1, 1), true);
    }

    private static Employee employee(Long id, String code, String firstName, String lastName, boolean active) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@example.com");
        employee.setCountry("India");
        employee.setDepartment("Engineering");
        employee.setJobTitle("Developer");
        employee.setHireDate(LocalDate.of(2024, 1, 1));
        employee.setActive(active);
        return employee;
    }

    private static EmployeeSalary salary(
            Employee employee, Long id, Double amount, String currency, LocalDate effectiveDate) {
        EmployeeSalary salary = new EmployeeSalary();
        salary.setId(id);
        salary.setEmployee(employee);
        salary.setBaseSalary(amount);
        salary.setCurrency(currency);
        salary.setEffectiveDate(effectiveDate);
        return salary;
    }
}
