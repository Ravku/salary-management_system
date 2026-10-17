package com.acme.hr.config;

import com.acme.hr.cache.EmployeeSearchPageCache;
import com.acme.hr.dto.AnalyticsSummary;
import com.acme.hr.dto.EmployeeDetailsResponse;
import com.acme.hr.dto.GroupAnalytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String EMPLOYEE_DETAILS = "employeeDetails";
    public static final String EMPLOYEE_SEARCH = "employeeSearch";
    public static final String ANALYTICS_SUMMARY = "analyticsSummary";
    public static final String ANALYTICS_BY_COUNTRY = "analyticsByCountry";
    public static final String ANALYTICS_BY_DEPARTMENT = "analyticsByDepartment";

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        Map<String, RedisCacheConfiguration> cacheConfigurations = cacheConfigurations(mapper);
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration(Duration.ofMinutes(5), typedSerializer(
                        mapper.constructType(Object.class), mapper)))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    Map<String, RedisSerializer<Object>> cacheSerializers() {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        return cacheSerializers(mapper);
    }

    private Map<String, RedisCacheConfiguration> cacheConfigurations(ObjectMapper mapper) {
        Map<String, RedisSerializer<Object>> serializers = cacheSerializers(mapper);
        return Map.of(
                EMPLOYEE_DETAILS, cacheConfiguration(Duration.ofMinutes(10), serializers.get(EMPLOYEE_DETAILS)),
                EMPLOYEE_SEARCH, cacheConfiguration(Duration.ofMinutes(2), serializers.get(EMPLOYEE_SEARCH)),
                ANALYTICS_SUMMARY, cacheConfiguration(Duration.ofMinutes(5), serializers.get(ANALYTICS_SUMMARY)),
                ANALYTICS_BY_COUNTRY, cacheConfiguration(Duration.ofMinutes(5), serializers.get(ANALYTICS_BY_COUNTRY)),
                ANALYTICS_BY_DEPARTMENT,
                cacheConfiguration(Duration.ofMinutes(5), serializers.get(ANALYTICS_BY_DEPARTMENT)));
    }

    private Map<String, RedisSerializer<Object>> cacheSerializers(ObjectMapper mapper) {
        JavaType groupAnalyticsList = mapper.getTypeFactory()
                .constructCollectionType(List.class, GroupAnalytics.class);
        return Map.of(
                EMPLOYEE_DETAILS, typedSerializer(mapper.constructType(EmployeeDetailsResponse.class), mapper),
                EMPLOYEE_SEARCH, typedSerializer(mapper.constructType(EmployeeSearchPageCache.class), mapper),
                ANALYTICS_SUMMARY, typedSerializer(mapper.constructType(AnalyticsSummary.class), mapper),
                ANALYTICS_BY_COUNTRY, typedSerializer(groupAnalyticsList, mapper),
                ANALYTICS_BY_DEPARTMENT, typedSerializer(groupAnalyticsList, mapper));
    }

    private RedisSerializer<Object> typedSerializer(JavaType type, ObjectMapper mapper) {
        return new JacksonJsonRedisSerializer<>(mapper, type);
    }

    @Bean
    public KeyGenerator employeeQueryKeyGenerator() {
        return (target, method, params) -> Stream.of(params)
                .map(value -> value == null ? "N" : "V" + encode(keyPart(value)))
                .collect(Collectors.joining(":"));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return cacheErrorHandler();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("read", cache, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logCacheError("write", cache, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("evict", cache, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logCacheError("clear", cache, exception);
            }
        };
    }

    private RedisCacheConfiguration cacheConfiguration(Duration ttl, RedisSerializer<?> valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "salary-management:v2:" + cacheName + "::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }

    private String keyPart(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof org.springframework.data.domain.Pageable pageable) {
            return pageable.getPageNumber() + "|" + pageable.getPageSize() + "|" + pageable.getSort();
        }
        return value.toString();
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private void logCacheError(String operation, Cache cache, RuntimeException exception) {
        logger.warn("Redis cache {} failed for cache '{}'; continuing with the database-backed operation.",
                operation, cache.getName(), exception);
    }
}
