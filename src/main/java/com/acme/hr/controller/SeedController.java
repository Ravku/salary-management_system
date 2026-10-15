package com.acme.hr.controller;

import com.acme.hr.service.SeedService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SeedController {

    private final SeedService seedService;

    public SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping("/seed")
    public String seedDatabase() {
        seedService.seedDatabase();
        return "Database seeding completed.";
    }
}
