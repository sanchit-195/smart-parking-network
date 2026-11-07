package com.smartparking.service;

import com.smartparking.dto.ParkingSlotResponseDto;
import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingEvent;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.InvalidStateTransitionException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingEventRepository;
import com.smartparking.repository.ParkingSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ParkingSlotServiceTest {

    @Autowired
    private ParkingSlotService parkingSlotService;

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
    @DisplayName("Reserve an AVAILABLE slot succeeds and logs SLOT_RESERVED event")
    void testReserveAvailableSlot() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P02").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        ParkingSlotResponseDto response = parkingSlotService.reserveSlot("P02");
        assertThat(response.getStatus()).isEqualTo(SlotStatus.RESERVED);

        ParkingSlot updated = parkingSlotRepository.findBySlotNumberIgnoreCase("P02").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.RESERVED);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.SLOT_RESERVED);
        assertThat(events.get(0).getPreviousStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(events.get(0).getNewStatus()).isEqualTo(SlotStatus.RESERVED);
    }

    @Test
    @DisplayName("Reserving an OCCUPIED or RESERVED slot throws InvalidStateTransitionException")
    void testReserveInvalidStateThrowsException() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P02").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        assertThatThrownBy(() -> parkingSlotService.reserveSlot("P02"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot reserve slot P02");

        slot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(slot);

        assertThatThrownBy(() -> parkingSlotService.reserveSlot("P02"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot reserve slot P02");
    }

    @Test
    @DisplayName("Cancel reservation on RESERVED slot succeeds and logs RESERVATION_CANCELLED event")
    void testCancelReservationSuccess() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P03").orElseThrow();
        slot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(slot);

        ParkingSlotResponseDto response = parkingSlotService.cancelReservation("P03");
        assertThat(response.getStatus()).isEqualTo(SlotStatus.AVAILABLE);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.RESERVATION_CANCELLED);
        assertThat(events.get(0).getPreviousStatus()).isEqualTo(SlotStatus.RESERVED);
        assertThat(events.get(0).getNewStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Cancel reservation on AVAILABLE slot throws InvalidStateTransitionException")
    void testCancelReservationOnAvailableThrowsException() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P03").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        assertThatThrownBy(() -> parkingSlotService.cancelReservation("P03"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot cancel reservation");
    }

    @Test
    @DisplayName("Release OCCUPIED slot succeeds and logs VEHICLE_EXIT event")
    void testReleaseOccupiedSlot() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P04").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        ParkingSlotResponseDto response = parkingSlotService.releaseSlot("P04");
        assertThat(response.getStatus()).isEqualTo(SlotStatus.AVAILABLE);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.VEHICLE_EXIT);
        assertThat(events.get(0).getPreviousStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(events.get(0).getNewStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Release non-occupied slot throws InvalidStateTransitionException")
    void testReleaseAvailableSlotThrowsException() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P04").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        assertThatThrownBy(() -> parkingSlotService.releaseSlot("P04"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot release slot P04");
    }

    @Test
    @DisplayName("Manual occupy on AVAILABLE slot succeeds")
    void testOccupyAvailableSlot() {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P06").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        ParkingSlotResponseDto response = parkingSlotService.occupySlot("P06");
        assertThat(response.getStatus()).isEqualTo(SlotStatus.OCCUPIED);

        List<ParkingEvent> events = parkingEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.VEHICLE_ENTRY);
    }

    @Test
    @DisplayName("Zone filtering returns 8 slots for valid zones and rejects invalid zone")
    void testZoneFiltering() {
        List<ParkingSlotResponseDto> zoneA = parkingSlotService.getSlotsByZone("A");
        assertThat(zoneA).hasSize(8);

        List<ParkingSlotResponseDto> zoneB = parkingSlotService.getSlotsByZone("b"); // Case-insensitive
        assertThat(zoneB).hasSize(8);

        List<ParkingSlotResponseDto> zoneC = parkingSlotService.getSlotsByZone("C");
        assertThat(zoneC).hasSize(8);

        assertThatThrownBy(() -> parkingSlotService.getSlotsByZone("Z"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid zone");
    }

    @Test
    @DisplayName("Get non-existent slot throws ResourceNotFoundException")
    void testGetNonExistentSlotThrowsException() {
        assertThatThrownBy(() -> parkingSlotService.getSlotByNumber("P99"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

