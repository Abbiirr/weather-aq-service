package com.dhakarun.application.shared;

public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
    }
}
