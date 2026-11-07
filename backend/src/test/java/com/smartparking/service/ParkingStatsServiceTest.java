package com.smartparking.service;

import com.smartparking.dto.OverallParkingStatsDto;
import com.smartparking.dto.ZoneStatsDto;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
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

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ParkingStatsServiceTest {

    @Autowired
    private ParkingStatsService parkingStatsService;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private DatabaseInitializationService databaseInitializationService;

    @BeforeEach
    void setUp() {
        databaseInitializationService.initializeSlotsIfEmpty();
        // Reset all to AVAILABLE
        List<ParkingSlot> allSlots = parkingSlotRepository.findAll();
        allSlots.forEach(s -> s.setStatus(SlotStatus.AVAILABLE));
        parkingSlotRepository.saveAll(allSlots);
    }

    @Test
    @DisplayName("Verify README example stats: 10 occupied, 3 reserved, 11 available -> 41.67% occupancy")
    void testOverallStatsCalculation() {
        List<ParkingSlot> allSlots = parkingSlotRepository.findAllByOrderBySlotNumberAsc();
        assertThat(allSlots).hasSize(24);

        // Mark 10 as OCCUPIED (slots 0 to 9)
        for (int i = 0; i < 10; i++) {
            allSlots.get(i).setStatus(SlotStatus.OCCUPIED);
        }

        // Mark 3 as RESERVED (slots 10 to 12)
        for (int i = 10; i < 13; i++) {
            allSlots.get(i).setStatus(SlotStatus.RESERVED);
        }

        // Remaining 11 stay AVAILABLE (slots 13 to 23)
        parkingSlotRepository.saveAll(allSlots);

        OverallParkingStatsDto stats = parkingStatsService.getOverallStats();
        assertThat(stats.getTotalSlots()).isEqualTo(24);
        assertThat(stats.getAvailableSlots()).isEqualTo(11);
        assertThat(stats.getOccupiedSlots()).isEqualTo(10);
        assertThat(stats.getReservedSlots()).isEqualTo(3);
        assertThat(stats.getOccupancyPercentage()).isEqualTo(41.67);
    }

    @Test
    @DisplayName("Verify zone-wise statistics for Zone A, Zone B, Zone C")
    void testZoneStatsCalculation() {
        // In Zone A (P01-P08), set P01 and P02 to OCCUPIED, P03 to RESERVED
        ParkingSlot p01 = parkingSlotRepository.findBySlotNumberIgnoreCase("P01").orElseThrow();
        p01.setStatus(SlotStatus.OCCUPIED);
        ParkingSlot p02 = parkingSlotRepository.findBySlotNumberIgnoreCase("P02").orElseThrow();
        p02.setStatus(SlotStatus.OCCUPIED);
        ParkingSlot p03 = parkingSlotRepository.findBySlotNumberIgnoreCase("P03").orElseThrow();
        p03.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.saveAll(List.of(p01, p02, p03));

        List<ZoneStatsDto> zoneStats = parkingStatsService.getZoneStats();
        assertThat(zoneStats).hasSize(3);

        ZoneStatsDto zoneA = zoneStats.stream().filter(z -> "A".equalsIgnoreCase(z.getZone())).findFirst().orElseThrow();
        assertThat(zoneA.getTotalSlots()).isEqualTo(8);
        assertThat(zoneA.getOccupiedSlots()).isEqualTo(2);
        assertThat(zoneA.getReservedSlots()).isEqualTo(1);
        assertThat(zoneA.getAvailableSlots()).isEqualTo(5);
        // (2 / 8) * 100 = 25.0%
        assertThat(zoneA.getOccupancyPercentage()).isEqualTo(25.0);

        ZoneStatsDto zoneB = zoneStats.stream().filter(z -> "B".equalsIgnoreCase(z.getZone())).findFirst().orElseThrow();
        assertThat(zoneB.getTotalSlots()).isEqualTo(8);
        assertThat(zoneB.getAvailableSlots()).isEqualTo(8);
        assertThat(zoneB.getOccupiedSlots()).isEqualTo(0);
        assertThat(zoneB.getOccupancyPercentage()).isEqualTo(0.0);
    }
}

