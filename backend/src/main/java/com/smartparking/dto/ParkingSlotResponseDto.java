package com.smartparking.dto;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a parking slot response.
 */
public class ParkingSlotResponseDto {

    private Long id;
    private String slotNumber;
    private String zone;
    private SlotStatus status;
    private String sensorId;
    private Double lastDistanceCm;
    private LocalDateTime lastSensorUpdate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ParkingSlotResponseDto() {
    }

    public ParkingSlotResponseDto(Long id, String slotNumber, String zone, SlotStatus status,
                                  String sensorId, Double lastDistanceCm,
                                  LocalDateTime lastSensorUpdate, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.zone = zone;
        this.status = status;
        this.sensorId = sensorId;
        this.lastDistanceCm = lastDistanceCm;
        this.lastSensorUpdate = lastSensorUpdate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ParkingSlotResponseDto fromEntity(ParkingSlot slot) {
        if (slot == null) {
            return null;
        }
        return new ParkingSlotResponseDto(
                slot.getId(),
                slot.getSlotNumber(),
                slot.getZone(),
                slot.getStatus(),
                slot.getSensorId(),
                slot.getLastDistanceCm(),
                slot.getLastSensorUpdate(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
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

