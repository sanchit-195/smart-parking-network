package com.smartparking.dto;

import com.smartparking.entity.SlotStatus;

/**
 * Data transfer object representing the result of processing an IoT sensor reading.
 */
public class SensorReadingResponseDto {

    private String sensorId;
    private String slotId;
    private Double distanceCm;
    private Boolean vehicleDetected;
    private SlotStatus slotStatus;
    private String message;

    public SensorReadingResponseDto() {
    }

    public SensorReadingResponseDto(String sensorId, String slotId, Double distanceCm,
                                    Boolean vehicleDetected, SlotStatus slotStatus, String message) {
        this.sensorId = sensorId;
        this.slotId = slotId;
        this.distanceCm = distanceCm;
        this.vehicleDetected = vehicleDetected;
        this.slotStatus = slotStatus;
        this.message = message;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public Double getDistanceCm() {
        return distanceCm;
    }

    public void setDistanceCm(Double distanceCm) {
        this.distanceCm = distanceCm;
    }

    public Boolean getVehicleDetected() {
        return vehicleDetected;
    }

    public void setVehicleDetected(Boolean vehicleDetected) {
        this.vehicleDetected = vehicleDetected;
    }

    public SlotStatus getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(SlotStatus slotStatus) {
        this.slotStatus = slotStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

