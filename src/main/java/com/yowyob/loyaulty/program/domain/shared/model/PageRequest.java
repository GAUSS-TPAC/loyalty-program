package com.yowyob.loyaulty.program.domain.shared.model;

public record PageRequest(int page, int size, String sortBy, String sortDirection) {

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, "createdAt", "DESC");
    }

    public static PageRequest first() {
        return new PageRequest(0, 20, "createdAt", "DESC");
    }

    public int offset() {
        return page * size;
    }
}
