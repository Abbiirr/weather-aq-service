CREATE TABLE IF NOT EXISTS air_quality (
    id BIGSERIAL PRIMARY KEY,
    location_id VARCHAR(64) NOT NULL REFERENCES locations(id),
    aqi INTEGER NOT NULL,
    pollutant_summary TEXT,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_air_quality_location_measured_at ON air_quality (location_id, measured_at DESC);
