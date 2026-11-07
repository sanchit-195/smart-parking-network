package com.smartparking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity representing an audit log / event recorded when a parking slot's status changes.
 */
@Entity
@Table(name = "parking_events")
public class ParkingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false, length = 10)
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private SlotStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private SlotStatus newStatus;

    @Column(name = "sensor_id", length = 10)
    private String sensorId;

    @Column(name = "distance_cm")
    private Double distanceCm;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public ParkingEvent() {
    }

    public ParkingEvent(String slotNumber, EventType eventType, SlotStatus previousStatus,
                        SlotStatus newStatus, String sensorId, Double distanceCm,
                        String description) {
        this.slotNumber = slotNumber;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.sensorId = sensorId;
        this.distanceCm = distanceCm;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
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

