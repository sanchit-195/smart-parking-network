-- PostgreSQL Database Setup Script for Smart Parking Network
-- Database name: smart_parking_db

-- 1. Create database (run as postgres superuser if the database does not exist)
-- CREATE DATABASE smart_parking_db;

-- 2. Connect to the database
-- \c smart_parking_db;

-- 3. The tables below will be generated automatically by Spring Boot (Hibernate ddl-auto=update).
-- The following DDL is provided for manual inspection or reference:

CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGSERIAL PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    zone VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sensor_id VARCHAR(10) NOT NULL UNIQUE,
    last_distance_cm DOUBLE PRECISION,
    last_sensor_update TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS parking_events (
    id BIGSERIAL PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    sensor_id VARCHAR(10),
    distance_cm DOUBLE PRECISION,
    description VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_parking_slots_zone ON parking_slots(zone);
CREATE INDEX IF NOT EXISTS idx_parking_slots_status ON parking_slots(status);
CREATE INDEX IF NOT EXISTS idx_parking_events_slot ON parking_events(slot_number);
CREATE INDEX IF NOT EXISTS idx_parking_events_timestamp ON parking_events(timestamp DESC);

