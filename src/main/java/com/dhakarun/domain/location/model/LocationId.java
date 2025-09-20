package com.dhakarun.domain.location.model;

public record LocationId(String value) {

    public LocationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LocationId cannot be blank");
        }
    }
}
