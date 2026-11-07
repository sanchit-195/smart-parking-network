package com.smartparking.dto;

/**
 * Data transfer object representing parking statistics for a specific zone.
 */
public class ZoneStatsDto {

    private String zone;
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private double occupancyPercentage;

    public ZoneStatsDto() {
    }

    public ZoneStatsDto(String zone, long totalSlots, long availableSlots, long occupiedSlots,
                        long reservedSlots, double occupancyPercentage) {
        this.zone = zone;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.occupiedSlots = occupiedSlots;
        this.reservedSlots = reservedSlots;
        this.occupancyPercentage = occupancyPercentage;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public long getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(long totalSlots) {
        this.totalSlots = totalSlots;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(long availableSlots) {
        this.availableSlots = availableSlots;
    }

    public long getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(long occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public long getReservedSlots() {
        return reservedSlots;
    }

    public void setReservedSlots(long reservedSlots) {
        this.reservedSlots = reservedSlots;
    }

    public double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }
}

