package com.smartparking.dto;

/**
 * Data transfer object representing overall facility parking statistics.
 */
public class OverallParkingStatsDto {

    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private double occupancyPercentage;

    public OverallParkingStatsDto() {
    }

    public OverallParkingStatsDto(long totalSlots, long availableSlots, long occupiedSlots,
                                 long reservedSlots, double occupancyPercentage) {
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.occupiedSlots = occupiedSlots;
        this.reservedSlots = reservedSlots;
        this.occupancyPercentage = occupancyPercentage;
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

