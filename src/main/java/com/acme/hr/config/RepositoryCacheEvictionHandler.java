package com.acme.hr.config;

import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RepositoryEventHandler
public class RepositoryCacheEvictionHandler {

    private static final List<String> AFFECTED_CACHES = List.of(
            CacheConfig.EMPLOYEE_DETAILS,
            CacheConfig.EMPLOYEE_SEARCH,
            CacheConfig.ANALYTICS_SUMMARY,
            CacheConfig.ANALYTICS_BY_COUNTRY,
            CacheConfig.ANALYTICS_BY_DEPARTMENT);

    private final CacheManager cacheManager;
    private final CacheErrorHandler cacheErrorHandler;

    public RepositoryCacheEvictionHandler(CacheManager cacheManager, CacheErrorHandler cacheErrorHandler) {
        this.cacheManager = cacheManager;
        this.cacheErrorHandler = cacheErrorHandler;
    }

    @HandleAfterCreate
    public void afterEmployeeCreate(Employee employee) {
        evictAffectedCaches();
    }

    @HandleAfterSave
    public void afterEmployeeSave(Employee employee) {
        evictAffectedCaches();
    }

    @HandleAfterDelete
    public void afterEmployeeDelete(Employee employee) {
        evictAffectedCaches();
    }

    @HandleAfterCreate
    public void afterSalaryCreate(EmployeeSalary salary) {
        evictAffectedCaches();
    }

    @HandleAfterSave
    public void afterSalarySave(EmployeeSalary salary) {
        evictAffectedCaches();
    }

    @HandleAfterDelete
    public void afterSalaryDelete(EmployeeSalary salary) {
        evictAffectedCaches();
    }

    private void evictAffectedCaches() {
        for (String cacheName : AFFECTED_CACHES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                throw new IllegalStateException("Cache is not configured: " + cacheName);
            }
            try {
                cache.clear();
            } catch (RuntimeException exception) {
                cacheErrorHandler.handleCacheClearError(exception, cache);
            }
        }
    }
}
