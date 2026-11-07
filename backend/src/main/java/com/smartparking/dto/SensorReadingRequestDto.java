package com.smartparking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data transfer object representing an incoming sensor reading from an IoT device / simulator.
 */
public class SensorReadingRequestDto {

    @NotBlank(message = "sensorId is required")
    private String sensorId;

    @NotBlank(message = "slotId is required")
    private String slotId;

    @NotNull(message = "distanceCm is required")
    @DecimalMin(value = "0.0", message = "distanceCm must be zero or positive")
    private Double distanceCm;

    public SensorReadingRequestDto() {
    }

    public SensorReadingRequestDto(String sensorId, String slotId, Double distanceCm) {
        this.sensorId = sensorId;
        this.slotId = slotId;
        this.distanceCm = distanceCm;
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
}

