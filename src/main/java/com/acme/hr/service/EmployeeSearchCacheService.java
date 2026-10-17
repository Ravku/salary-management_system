package com.acme.hr.service;

import com.acme.hr.cache.EmployeeSearchPageCache;
import com.acme.hr.config.CacheConfig;
import com.acme.hr.dto.EmployeeDetailsResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeSearchCacheService {

    private final EmployeeService employeeService;

    public EmployeeSearchCacheService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Cacheable(value = CacheConfig.EMPLOYEE_SEARCH, keyGenerator = "employeeQueryKeyGenerator")
    public EmployeeSearchPageCache findEmployees(
            String search, String country, String department, String jobTitle, Boolean active, Pageable pageable) {
        Page<EmployeeDetailsResponse> page = employeeService.findEmployees(
                search, country, department, jobTitle, active, pageable);
        return EmployeeSearchPageCache.from(page);
    }
}
