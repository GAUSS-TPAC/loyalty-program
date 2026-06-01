package com.yowyob.loyalty.domain.shared.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;

public record PageRequest(
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
    public PageRequest {
        if (page < 0) {
            throw new DomainValidationException("La page ne doit pas être négative");
        }
        if (size < 1 || size > 100) {
            throw new DomainValidationException("La taille doit être entre 1 et 100");
        }
        if (sortDirection != null && !sortDirection.equals("ASC") && !sortDirection.equals("DESC")) {
            throw new DomainValidationException("La direction du tri doit être ASC ou DESC");
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, "ASC");
    }

    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        return new PageRequest(page, size, sortBy, sortDirection);
    }

    public int offset() {
        return page * size;
    }
}
