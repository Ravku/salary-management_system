package com.acme.hr.service;

import com.acme.hr.config.SeedDataConfig;
import com.acme.hr.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class SeedService {

    private final SeedDataConfig seedDataConfig;

    public SeedService(SeedDataConfig seedDataConfig) {
        this.seedDataConfig = seedDataConfig;
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.EMPLOYEE_DETAILS, allEntries = true),
            @CacheEvict(value = CacheConfig.EMPLOYEE_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConfig.ANALYTICS_SUMMARY, allEntries = true),
            @CacheEvict(value = CacheConfig.ANALYTICS_BY_COUNTRY, allEntries = true),
            @CacheEvict(value = CacheConfig.ANALYTICS_BY_DEPARTMENT, allEntries = true)
    })
    public void seedDatabase() {
        seedDataConfig.seedDatabase();
    }
}
