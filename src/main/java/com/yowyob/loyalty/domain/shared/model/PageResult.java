package com.yowyob.loyalty.domain.shared.model;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {
    private final List<T> content;
    private final long total;
    private final int page;
    private final int size;

    public PageResult(List<T> content, long total, int page, int size) {
        this.content = content != null ? List.copyOf(content) : Collections.emptyList();
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int totalPages() {
        if (size == 0) return 0;
        return (int) Math.ceil((double) total / size);
    }

    public boolean hasNext() {
        return page < totalPages() - 1;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), 0, page, size);
    }
}
