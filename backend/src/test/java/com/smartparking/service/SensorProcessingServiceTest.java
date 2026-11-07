package com.smartparking.service;

import com.smartparking.dto.SensorReadingRequestDto;
import com.smartparking.dto.SensorReadingResponseDto;
import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingEvent;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.InvalidSensorMappingException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingEventRepository;
import com.smartparking.repository.ParkingSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SensorProcessingServiceTest {

    @Autowired
    private SensorProcessingService sensorProcessingService;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private ParkingEventRepository parkingEventRepository;

    @Autowired
    private DatabaseInitializationService databaseInitializationService;

    @BeforeEach
    void setUp() {
        databaseInitializationService.initializeSlotsIfEmpty();
        parkingEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Sensor reading < 15 cm marks AVAILABLE slot as OCCUPIED and logs VEHICLE_ENTRY")
    void testVehicleEntryOnAvailableSlot() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        SensorReadingRequestDto request = new SensorReadingRequestDto("S05", "P05", 8.2);
        SensorReadingResponseDto response = sensorProcessingService.processSensorReading(request);

        assertThat(response.getVehicleDetected()).isTrue();
        assertThat(response.getSlotStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(response.getDistanceCm()).isEqualTo(8.2);
        assertThat(response.getMessage()).contains("Vehicle detected");

        // Verify database state
        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(updated.getLastDistanceCm()).isEqualTo(8.2);
        assertThat(updated.getLastSensorUpdate()).isNotNull();

        // Verify event logged
        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        ParkingEvent event = events.get(0);
        assertThat(event.getSlotNumber()).isEqualTo("P05");
        assertThat(event.getEventType()).isEqualTo(EventType.VEHICLE_ENTRY);
        assertThat(event.getPreviousStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(event.getNewStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(event.getSensorId()).isEqualTo("S05");
        assertThat(event.getDistanceCm()).isEqualTo(8.2);
    }

    @Test
    @DisplayName("Sensor reading >= 15 cm marks OCCUPIED slot as AVAILABLE and logs VEHICLE_EXIT")
    void testVehicleExitOnOccupiedSlot() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        SensorReadingRequestDto request = new SensorReadingRequestDto("S05", "P05", 30.0);
        SensorReadingResponseDto response = sensorProcessingService.processSensorReading(request);

        assertThat(response.getVehicleDetected()).isFalse();
        assertThat(response.getSlotStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(response.getMessage()).contains("Vehicle left");

        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(updated.getLastDistanceCm()).isEqualTo(30.0);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        ParkingEvent event = events.get(0);
        assertThat(event.getEventType()).isEqualTo(EventType.VEHICLE_EXIT);
        assertThat(event.getPreviousStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(event.getNewStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Repeated readings without state change do NOT create duplicate events")
    void testRepeatedReadingsDoNotCreateDuplicateEvents() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        // Vehicle already present, another < 15 reading arrives
        SensorReadingRequestDto reading1 = new SensorReadingRequestDto("S05", "P05", 7.5);
        SensorReadingResponseDto response1 = sensorProcessingService.processSensorReading(reading1);
        assertThat(response1.getSlotStatus()).isEqualTo(SlotStatus.OCCUPIED);

        SensorReadingRequestDto reading2 = new SensorReadingRequestDto("S05", "P05", 7.8);
        SensorReadingResponseDto response2 = sensorProcessingService.processSensorReading(reading2);
        assertThat(response2.getSlotStatus()).isEqualTo(SlotStatus.OCCUPIED);

        // No events should be created because status remained OCCUPIED
        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).isEmpty();

        // But slot distance should still update to latest
        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        assertThat(updated.getLastDistanceCm()).isEqualTo(7.8);
    }

    @Test
    @DisplayName("A RESERVED slot remains RESERVED when sensor reports no vehicle (>= 15 cm)")
    void testReservedSlotRemainsReservedWhenNoVehicleDetected() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(slot);

        SensorReadingRequestDto request = new SensorReadingRequestDto("S05", "P05", 45.0);
        SensorReadingResponseDto response = sensorProcessingService.processSensorReading(request);

        assertThat(response.getVehicleDetected()).isFalse();
        assertThat(response.getSlotStatus()).isEqualTo(SlotStatus.RESERVED);
        assertThat(response.getMessage()).contains("remains reserved");

        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.RESERVED);

        // No event created
        assertThat(parkingEventRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("A vehicle entering a RESERVED slot changes its status to OCCUPIED and logs VEHICLE_ENTRY")
    void testVehicleEnteringReservedSlotBecomesOccupied() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(slot);

        SensorReadingRequestDto request = new SensorReadingRequestDto("S05", "P05", 10.5);
        SensorReadingResponseDto response = sensorProcessingService.processSensorReading(request);

        assertThat(response.getVehicleDetected()).isTrue();
        assertThat(response.getSlotStatus()).isEqualTo(SlotStatus.OCCUPIED);

        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.OCCUPIED);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.VEHICLE_ENTRY);
        assertThat(events.get(0).getPreviousStatus()).isEqualTo(SlotStatus.RESERVED);
        assertThat(events.get(0).getNewStatus()).isEqualTo(SlotStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Invalid sensor-to-slot mapping throws InvalidSensorMappingException")
    void testMismatchedSensorThrowsException() {
        // P05 is mapped to S05, not S06
        SensorReadingRequestDto request = new SensorReadingRequestDto("S06", "P05", 8.0);

        assertThatThrownBy(() -> sensorProcessingService.processSensorReading(request))
                .isInstanceOf(InvalidSensorMappingException.class)
                .hasMessageContaining("Sensor S06 is not mapped to slot P05");
    }

    @Test
    @DisplayName("Non-existent slot ID throws ResourceNotFoundException")
    void testNonExistentSlotThrowsException() {
        SensorReadingRequestDto request = new SensorReadingRequestDto("S99", "P99", 8.0);

        assertThatThrownBy(() -> sensorProcessingService.processSensorReading(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Negative distance throws BadRequestException")
    void testNegativeDistanceThrowsException() {
        SensorReadingRequestDto request = new SensorReadingRequestDto("S05", "P05", -5.0);

        assertThatThrownBy(() -> sensorProcessingService.processSensorReading(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Distance cannot be negative");
    }
}

