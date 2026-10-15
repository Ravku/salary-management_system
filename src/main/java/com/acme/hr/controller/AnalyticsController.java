package com.acme.hr.controller;

import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.GroupAnalytics;
import com.acme.hr.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummary getSummary() {
        return analyticsService.summary();
    }

    @GetMapping("/by-country")
    public List<GroupAnalytics> getByCountry() {
        return analyticsService.byCountry();
    }

    @GetMapping("/by-department")
    public List<GroupAnalytics> getByDepartment() {
        return analyticsService.byDepartment();
    }
}
