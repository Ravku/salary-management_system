package com.acme.hr.cache;

import com.acme.hr.dto.EmployeeDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public record EmployeeSearchPageCache(
        List<EmployeeDetailsResponse> content,
        boolean paged,
        int pageNumber,
        int pageSize,
        long totalElements,
        List<SortOrder> sortOrders
) {

    public static EmployeeSearchPageCache from(Page<EmployeeDetailsResponse> page) {
        List<SortOrder> sortOrders = page.getSort().stream()
                .map(order -> new SortOrder(
                        order.getProperty(),
                        order.getDirection().name(),
                        order.isIgnoreCase(),
                        order.getNullHandling().name()))
                .toList();
        boolean paged = page.getPageable().isPaged();
        return new EmployeeSearchPageCache(
                page.getContent(),
                paged,
                paged ? page.getNumber() : 0,
                paged ? page.getSize() : 0,
                page.getTotalElements(),
                sortOrders);
    }

    public Page<EmployeeDetailsResponse> toPage() {
        Sort sort = Sort.by(sortOrders.stream().map(SortOrder::toOrder).toList());
        Pageable pageable = paged
                ? PageRequest.of(pageNumber, pageSize, sort)
                : Pageable.unpaged(sort);
        return new PageImpl<>(content, pageable, totalElements);
    }

    public record SortOrder(String property, String direction, boolean ignoreCase, String nullHandling) {

        private Sort.Order toOrder() {
            Sort.Order order = new Sort.Order(Sort.Direction.valueOf(direction), property);
            order = switch (Sort.NullHandling.valueOf(nullHandling)) {
                case NULLS_FIRST -> order.nullsFirst();
                case NULLS_LAST -> order.nullsLast();
                case NATIVE -> order.nullsNative();
            };
            return ignoreCase ? order.ignoreCase() : order;
        }
    }
}
