package com.smartparking.dto;

import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingEvent;
import com.smartparking.entity.SlotStatus;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a historical parking event / activity.
 */
public class ParkingEventResponseDto {

    private Long id;
    private String slotNumber;
    private EventType eventType;
    private SlotStatus previousStatus;
    private SlotStatus newStatus;
    private String sensorId;
    private Double distanceCm;
    private String description;
    private LocalDateTime timestamp;

    public ParkingEventResponseDto() {
    }

    public ParkingEventResponseDto(Long id, String slotNumber, EventType eventType,
                                  SlotStatus previousStatus, SlotStatus newStatus,
                                  String sensorId, Double distanceCm, String description,
                                  LocalDateTime timestamp) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.sensorId = sensorId;
        this.distanceCm = distanceCm;
        this.description = description;
        this.timestamp = timestamp;
    }

    public static ParkingEventResponseDto fromEntity(ParkingEvent event) {
        if (event == null) {
            return null;
        }
        return new ParkingEventResponseDto(
                event.getId(),
                event.getSlotNumber(),
                event.getEventType(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getSensorId(),
                event.getDistanceCm(),
                event.getDescription(),
                event.getTimestamp()
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public SlotStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(SlotStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public SlotStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(SlotStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Double getDistanceCm() {
        return distanceCm;
    }

    public void setDistanceCm(Double distanceCm) {
        this.distanceCm = distanceCm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

