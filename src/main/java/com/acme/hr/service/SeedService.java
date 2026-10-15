package com.acme.hr.service;

import com.acme.hr.config.SeedDataConfig;
import org.springframework.stereotype.Service;

@Service
public class SeedService {

    private final SeedDataConfig seedDataConfig;

    public SeedService(SeedDataConfig seedDataConfig) {
        this.seedDataConfig = seedDataConfig;
    }

    public void seedDatabase() {
        seedDataConfig.seedDatabase();
    }
}
