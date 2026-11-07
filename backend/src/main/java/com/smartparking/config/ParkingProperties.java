package com.smartparking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for smart parking IoT sensor behavior.
 */
@Configuration
@ConfigurationProperties(prefix = "parking.sensor")
public class ParkingProperties {

    /**
     * Distance threshold in centimeters.
     * Distance strictly below this value indicates vehicle presence (OCCUPIED).
     * Distance greater than or equal to this value indicates vehicle absence.
     */
    private double distanceThresholdCm = 15.0;

    public double getDistanceThresholdCm() {
        return distanceThresholdCm;
    }

    public void setDistanceThresholdCm(double distanceThresholdCm) {
        this.distanceThresholdCm = distanceThresholdCm;
    }
}

