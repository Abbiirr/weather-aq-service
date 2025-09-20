package com.dhakarun.domain.location.model;

import java.util.Objects;

public final class Location {

    private final LocationId id;
    private final String name;
    private final Coordinates coordinates;
    private final LocationType type;

    public Location(LocationId id, String name, Coordinates coordinates, LocationType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates");
        this.type = Objects.requireNonNull(type, "type");
    }

    public LocationId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocationType getType() {
        return type;
    }
}
