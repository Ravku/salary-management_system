package com.acme.hr.controller;

import com.acme.hr.service.SeedService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SeedControllerTest {

    @Test
    void seedDatabaseDelegatesAndReturnsCompletionMessage() {
        SeedService seedService = mock(SeedService.class);
        SeedController controller = new SeedController(seedService);

        assertEquals("Database seeding completed.", controller.seedDatabase());
        verify(seedService).seedDatabase();
    }
}
