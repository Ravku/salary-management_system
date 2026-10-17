package com.acme.hr.config;

import com.acme.hr.cache.EmployeeSearchPageCache;
import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.EmployeeSalaryResponse;
import com.acme.hr.dto.GroupAnalytics;
import com.acme.hr.dto.SalaryStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final Map<String, RedisSerializer<Object>> serializers = new CacheConfig().cacheSerializers();

    @Test
    void serializesEmployeeDtoIncludingDates() {
        EmployeeDetailsResponse employee = employee();

        Object restored = roundTrip(CacheConfig.EMPLOYEE_DETAILS, employee);

        assertThat(restored).isEqualTo(employee);
    }

    @Test
    void serializesPagedEmployeeSearchResult() {
        Sort sort = Sort.by(Sort.Order.desc("firstName").ignoreCase().nullsLast());
        EmployeeSearchPageCache page = EmployeeSearchPageCache.from(new PageImpl<>(
                List.of(employee()), PageRequest.of(0, 20, sort), 1));

        Object restored = roundTrip(CacheConfig.EMPLOYEE_SEARCH, page);

        assertThat(restored).isInstanceOf(EmployeeSearchPageCache.class);
        var restoredPage = ((EmployeeSearchPageCache) restored).toPage();
        assertThat(restoredPage.getContent()).containsExactly(employee());
        assertThat(restoredPage.getTotalElements()).isEqualTo(1);
        assertThat(restoredPage.getNumber()).isZero();
        assertThat(restoredPage.getSize()).isEqualTo(20);
        assertThat(restoredPage.getSort()).isEqualTo(sort);
    }

    @Test
    void serializesAnalyticsSummaryAndListsWithConcreteTypes() {
        AnalyticsSummary summary = new AnalyticsSummary(
                2, 1, List.of(new SalaryStatistics("USD", 2, 75.5, 151.0)));
        List<GroupAnalytics> groups = List.of(
                new GroupAnalytics("Engineering", 2, 1, summary.salaryStatistics()));

        assertThat(roundTrip(CacheConfig.ANALYTICS_SUMMARY, summary)).isEqualTo(summary);
        assertThat(roundTrip(CacheConfig.ANALYTICS_BY_COUNTRY, groups)).isEqualTo(groups);
        assertThat(roundTrip(CacheConfig.ANALYTICS_BY_DEPARTMENT, groups)).isEqualTo(groups);
    }

    private Object roundTrip(String cacheName, Object value) {
        RedisSerializer<Object> serializer = serializers.get(cacheName);
        return serializer.deserialize(serializer.serialize(value));
    }

    private EmployeeDetailsResponse employee() {
        return new EmployeeDetailsResponse(
                7L,
                "EMP7",
                "Asha",
                "Patel",
                "asha@example.com",
                "India",
                "Engineering",
                "Developer",
                LocalDate.of(2025, 1, 15),
                true,
                new EmployeeSalaryResponse(11L, 95000.0, "INR", LocalDate.of(2026, 10, 1)));
    }
}
