package com.acme.hr.controller;

import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.EmployeeRequest;
import com.acme.hr.dto.SalaryUpdateRequest;
import com.acme.hr.cache.EmployeeSearchPageCache;
import com.acme.hr.service.EmployeeSearchCacheService;
import com.acme.hr.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    private EmployeeService employeeService;
    private EmployeeSearchCacheService employeeSearchCacheService;
    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        employeeService = mock(EmployeeService.class);
        employeeSearchCacheService = mock(EmployeeSearchCacheService.class);
        controller = new EmployeeController(employeeService, employeeSearchCacheService);
    }

    @Test
    void getEmployeesPassesFiltersAndPaginationToService() {
        PageRequest pageable = PageRequest.of(1, 5);
        Page<EmployeeDetailsResponse> page = new PageImpl<>(List.of(), pageable, 0);
        when(employeeSearchCacheService.findEmployees(
                "Asha", "India", "Engineering", "Developer", true, pageable))
                .thenReturn(EmployeeSearchPageCache.from(page));

        assertEquals(page, controller.getEmployees(
                "Asha", "India", "Engineering", "Developer", true, pageable));
        verify(employeeSearchCacheService).findEmployees(
                "Asha", "India", "Engineering", "Developer", true, pageable);
    }

    @Test
    void getEmployeeDelegatesId() {
        EmployeeDetailsResponse response = response();
        when(employeeService.getEmployee(12L)).thenReturn(response);

        assertEquals(response, controller.getEmployee(12L));
        verify(employeeService).getEmployee(12L);
    }

    @Test
    void createEmployeeDelegatesRequestAndReturnsCreatedStatus() throws NoSuchMethodException {
        EmployeeRequest request = request();
        EmployeeDetailsResponse response = response();
        when(employeeService.createEmployee(request)).thenReturn(response);

        assertEquals(response, controller.createEmployee(request));
        assertEquals(HttpStatus.CREATED, EmployeeController.class
                .getMethod("createEmployee", EmployeeRequest.class)
                .getAnnotation(ResponseStatus.class).value());
        verify(employeeService).createEmployee(request);
    }

    @Test
    void updateEmployeeDelegatesIdAndRequest() {
        EmployeeRequest request = request();
        EmployeeDetailsResponse response = response();
        when(employeeService.updateEmployee(12L, request)).thenReturn(response);

        assertEquals(response, controller.updateEmployee(12L, request));
        verify(employeeService).updateEmployee(12L, request);
    }

    @Test
    void updateSalaryDelegatesIdAndRequest() {
        SalaryUpdateRequest request = new SalaryUpdateRequest(95000.0, "USD", LocalDate.of(2026, 1, 1));
        EmployeeDetailsResponse response = response();
        when(employeeService.updateSalary(12L, request)).thenReturn(response);

        assertEquals(response, controller.updateSalary(12L, request));
        verify(employeeService).updateSalary(12L, request);
    }

    private static EmployeeRequest request() {
        return new EmployeeRequest("EMP12", "Asha", "Patel", "asha@example.com",
                "India", "Engineering", "Developer", LocalDate.of(2025, 1, 1), true);
    }

    private static EmployeeDetailsResponse response() {
        return new EmployeeDetailsResponse(12L, "EMP12", "Asha", "Patel", "asha@example.com",
                "India", "Engineering", "Developer", LocalDate.of(2025, 1, 1), true, null);
    }
}
