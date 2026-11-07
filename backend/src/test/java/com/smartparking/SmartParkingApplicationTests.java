package com.smartparking;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.service.DatabaseInitializationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SmartParkingApplicationTests {

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private DatabaseInitializationService databaseInitializationService;

    @Test
    @DisplayName("Context loads and exactly 24 parking slots are initialized")
    void contextLoadsAndInitializes24Slots() {
        long count = parkingSlotRepository.count();
        assertThat(count).isEqualTo(24);

        // Zone A: P01 to P08
        List<ParkingSlot> zoneA = parkingSlotRepository.findByZoneIgnoreCaseOrderBySlotNumberAsc("A");
        assertThat(zoneA).hasSize(8);
        for (int i = 1; i <= 8; i++) {
            String expectedSlot = String.format("P%02d", i);
            String expectedSensor = String.format("S%02d", i);
            ParkingSlot slot = zoneA.get(i - 1);
            assertThat(slot.getSlotNumber()).isEqualTo(expectedSlot);
            assertThat(slot.getSensorId()).isEqualTo(expectedSensor);
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        }

        // Zone B: P09 to P16
        List<ParkingSlot> zoneB = parkingSlotRepository.findByZoneIgnoreCaseOrderBySlotNumberAsc("B");
        assertThat(zoneB).hasSize(8);
        for (int i = 9; i <= 16; i++) {
            String expectedSlot = String.format("P%02d", i);
            String expectedSensor = String.format("S%02d", i);
            ParkingSlot slot = zoneB.get(i - 9);
            assertThat(slot.getSlotNumber()).isEqualTo(expectedSlot);
            assertThat(slot.getSensorId()).isEqualTo(expectedSensor);
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        }

        // Zone C: P17 to P24
        List<ParkingSlot> zoneC = parkingSlotRepository.findByZoneIgnoreCaseOrderBySlotNumberAsc("C");
        assertThat(zoneC).hasSize(8);
        for (int i = 17; i <= 24; i++) {
            String expectedSlot = String.format("P%02d", i);
            String expectedSensor = String.format("S%02d", i);
            ParkingSlot slot = zoneC.get(i - 17);
            assertThat(slot.getSlotNumber()).isEqualTo(expectedSlot);
            assertThat(slot.getSensorId()).isEqualTo(expectedSensor);
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        }
    }

    @Test
    @DisplayName("Initialization service is idempotent and does not create duplicate slots on subsequent runs")
    void initializationIsIdempotent() {
        long countBefore = parkingSlotRepository.count();
        databaseInitializationService.initializeSlotsIfEmpty();
        long countAfter = parkingSlotRepository.count();

        assertThat(countAfter).isEqualTo(countBefore);
    }
}

