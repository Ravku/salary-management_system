package com.acme.hr.service;

import com.acme.hr.config.SeedDataConfig;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SeedServiceTest {

    @Test
    void delegatesDatabaseSeedingToConfiguration() {
        SeedDataConfig seedDataConfig = mock(SeedDataConfig.class);
        SeedService seedService = new SeedService(seedDataConfig);

        seedService.seedDatabase();

        verify(seedDataConfig).seedDatabase();
    }
}
