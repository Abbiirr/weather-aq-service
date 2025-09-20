package com.dhakarun.application.shared;

import java.util.List;

public record PageResult<T>(int page, int size, long totalElements, boolean hasNext, List<T> content) {

    public PageResult {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        content = List.copyOf(content);
    }
}
