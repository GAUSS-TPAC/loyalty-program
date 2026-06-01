package com.yowyob.loyaulty.program.domain.shared.model;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, totalElements, page, size, totalPages);
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), 0L, page, size, 0);
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public boolean isLast() {
        return page >= totalPages - 1;
    }
}
