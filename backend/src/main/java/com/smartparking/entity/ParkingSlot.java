package com.smartparking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a physical parking slot and its virtual IoT sensor mapping.
 */
@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", unique = true, nullable = false, length = 10)
    private String slotNumber;

    @Column(name = "zone", nullable = false, length = 10)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlotStatus status;

    @Column(name = "sensor_id", unique = true, nullable = false, length = 10)
    private String sensorId;

    @Column(name = "last_distance_cm")
    private Double lastDistanceCm;

    @Column(name = "last_sensor_update")
    private LocalDateTime lastSensorUpdate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ParkingSlot() {
    }

    public ParkingSlot(String slotNumber, String zone, SlotStatus status, String sensorId) {
        this.slotNumber = slotNumber;
        this.zone = zone;
        this.status = status;
        this.sensorId = sensorId;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Double getLastDistanceCm() {
        return lastDistanceCm;
    }

    public void setLastDistanceCm(Double lastDistanceCm) {
        this.lastDistanceCm = lastDistanceCm;
    }

    public LocalDateTime getLastSensorUpdate() {
        return lastSensorUpdate;
    }

    public void setLastSensorUpdate(LocalDateTime lastSensorUpdate) {
        this.lastSensorUpdate = lastSensorUpdate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

