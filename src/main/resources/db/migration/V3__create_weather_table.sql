CREATE TABLE IF NOT EXISTS weather (
    id BIGSERIAL PRIMARY KEY,
    location_id VARCHAR(64) NOT NULL REFERENCES locations(id),
    temperature_celsius DOUBLE PRECISION NOT NULL,
    humidity_percentage DOUBLE PRECISION NOT NULL,
    wind_speed_mps DOUBLE PRECISION NOT NULL,
    wind_direction VARCHAR(32) NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_weather_location_measured_at ON weather (location_id, measured_at DESC);
