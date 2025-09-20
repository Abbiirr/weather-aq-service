package com.dhakarun.adapter.in.web.dto;

import java.util.List;

public record PageResponse<T>(
    int page,
    int size,
    long totalElements,
    List<T> content
) {
}
