package com.example.selfhealthcare.util;

import com.example.selfhealthcare.dto.PagedResponse;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public final class PagingSupport {

    private static final int DEFAULT_SIZE = 10;

    private PagingSupport() {
    }

    public static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    public static int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, DEFAULT_SIZE);
    }

    public static <T, R> PagedResponse<R> toResponse(Page<T> page, int normalizedPage, Function<T, R> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                normalizedPage,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages() == 0 ? 1 : page.getTotalPages());
    }
}
