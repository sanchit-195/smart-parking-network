package com.smartparking.service;

import com.smartparking.config.ParkingProperties;
import com.smartparking.dto.SensorReadingRequestDto;
import com.smartparking.dto.SensorReadingResponseDto;
import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.InvalidSensorMappingException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service processing IoT ultrasonic sensor readings and updating parking slot states.
 */
@Service
public class SensorProcessingService {

    private static final Logger log = LoggerFactory.getLogger(SensorProcessingService.class);

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingEventService parkingEventService;
    private final ParkingProperties parkingProperties;

    public SensorProcessingService(ParkingSlotRepository parkingSlotRepository,
                                  ParkingEventService parkingEventService,
                                  ParkingProperties parkingProperties) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingEventService = parkingEventService;
        this.parkingProperties = parkingProperties;
    }

    @Transactional
    public SensorReadingResponseDto processSensorReading(SensorReadingRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Sensor reading request cannot be null.");
        }

        String rawSlotId = request.getSlotId();
        String rawSensorId = request.getSensorId();
        Double distanceCm = request.getDistanceCm();

        if (rawSlotId == null || rawSlotId.trim().isEmpty()) {
            throw new BadRequestException("Slot ID is required.");
        }
        if (rawSensorId == null || rawSensorId.trim().isEmpty()) {
            throw new BadRequestException("Sensor ID is required.");
        }
        if (distanceCm == null) {
            throw new BadRequestException("Distance reading in cm is required.");
        }
        if (distanceCm < 0) {
            throw new BadRequestException("Distance cannot be negative. Provided: " + distanceCm);
        }

        String slotId = rawSlotId.trim().toUpperCase();
        String sensorId = rawSensorId.trim().toUpperCase();

        // 1. Verify slot exists
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + slotId));

        // 2. Verify sensor mapping
        if (!slot.getSensorId().equalsIgnoreCase(sensorId)) {
            throw new InvalidSensorMappingException(
                    String.format("Sensor %s is not mapped to slot %s. Expected sensor: %s",
                            sensorId, slot.getSlotNumber(), slot.getSensorId()));
        }

        // 3. Evaluate threshold
        double threshold = parkingProperties.getDistanceThresholdCm();
        boolean vehicleDetected = distanceCm < threshold;

        // 4. Update sensor reading metadata on slot
        slot.setLastDistanceCm(distanceCm);
        slot.setLastSensorUpdate(LocalDateTime.now());
        slot.setUpdatedAt(LocalDateTime.now());

        SlotStatus currentStatus = slot.getStatus();
        String message;

        // 5. Evaluate state transitions according to README rules
        switch (currentStatus) {
            case AVAILABLE:
                if (vehicleDetected) {
                    slot.setStatus(SlotStatus.OCCUPIED);
                    parkingEventService.recordEvent(
                            slot.getSlotNumber(),
                            EventType.VEHICLE_ENTRY,
                            currentStatus,
                            SlotStatus.OCCUPIED,
                            sensorId,
                            distanceCm,
                            "Vehicle detected. Slot marked OCCUPIED."
                    );
                    message = "Vehicle detected. Parking slot marked as occupied.";
                } else {
                    message = "No vehicle detected. Parking slot remains available.";
                }
                break;

            case OCCUPIED:
                if (!vehicleDetected) {
                    slot.setStatus(SlotStatus.AVAILABLE);
                    parkingEventService.recordEvent(
                            slot.getSlotNumber(),
                            EventType.VEHICLE_EXIT,
                            currentStatus,
                            SlotStatus.AVAILABLE,
                            sensorId,
                            distanceCm,
                            "Vehicle left. Slot marked AVAILABLE."
                    );
                    message = "Vehicle left. Parking slot marked as available.";
                } else {
                    message = "Vehicle detected. Parking slot remains occupied.";
                }
                break;

            case RESERVED:
                if (vehicleDetected) {
                    slot.setStatus(SlotStatus.OCCUPIED);
                    parkingEventService.recordEvent(
                            slot.getSlotNumber(),
                            EventType.VEHICLE_ENTRY,
                            currentStatus,
                            SlotStatus.OCCUPIED,
                            sensorId,
                            distanceCm,
                            "Vehicle detected in reserved slot. Slot marked OCCUPIED."
                    );
                    message = "Vehicle detected. Parking slot marked as occupied.";
                } else {
                    // Stays RESERVED when no vehicle detected
                    message = "No vehicle detected. Parking slot remains reserved.";
                }
                break;

            default:
                message = "Reading recorded.";
                break;
        }

        parkingSlotRepository.save(slot);

        log.info("Processed reading for slot {}: sensor={}, distance={}cm, threshold={}cm, vehicleDetected={}, status={}",
                slot.getSlotNumber(), sensorId, distanceCm, threshold, vehicleDetected, slot.getStatus());

        return new SensorReadingResponseDto(
                slot.getSensorId(),
                slot.getSlotNumber(),
                distanceCm,
                vehicleDetected,
                slot.getStatus(),
                message
        );
    }
}

