package com.acme.hr.controller;

import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.GroupAnalytics;
import com.acme.hr.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);
        controller = new AnalyticsController(analyticsService);
    }

    @Test
    void summaryDelegatesToService() {
        AnalyticsSummary summary = new AnalyticsSummary(10, 8, List.of());
        when(analyticsService.summary()).thenReturn(summary);

        assertEquals(summary, controller.getSummary());
        verify(analyticsService).summary();
    }

    @Test
    void byCountryDelegatesToService() {
        List<GroupAnalytics> groups = List.of(new GroupAnalytics("India", 3, 2, List.of()));
        when(analyticsService.byCountry()).thenReturn(groups);

        assertEquals(groups, controller.getByCountry());
        verify(analyticsService).byCountry();
    }

    @Test
    void byDepartmentDelegatesToService() {
        List<GroupAnalytics> groups = List.of(new GroupAnalytics("Engineering", 3, 2, List.of()));
        when(analyticsService.byDepartment()).thenReturn(groups);

        assertEquals(groups, controller.getByDepartment());
        verify(analyticsService).byDepartment();
    }
}
