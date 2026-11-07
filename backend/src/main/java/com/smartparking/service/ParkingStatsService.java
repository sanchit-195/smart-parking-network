package com.smartparking.service;

import com.smartparking.dto.OverallParkingStatsDto;
import com.smartparking.dto.ZoneStatsDto;
import com.smartparking.entity.SlotStatus;
import com.smartparking.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service calculating overall and zone-wise parking capacity and occupancy statistics.
 */
@Service
public class ParkingStatsService {

    private static final List<String> ZONES = List.of("A", "B", "C");

    private final ParkingSlotRepository parkingSlotRepository;

    public ParkingStatsService(ParkingSlotRepository parkingSlotRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
    }

    @Transactional(readOnly = true)
    public OverallParkingStatsDto getOverallStats() {
        long totalSlots = parkingSlotRepository.count();
        long availableSlots = parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE);
        long occupiedSlots = parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED);
        long reservedSlots = parkingSlotRepository.countByStatus(SlotStatus.RESERVED);

        double occupancyPercentage = calculateOccupancyPercentage(occupiedSlots, totalSlots);

        return new OverallParkingStatsDto(
                totalSlots,
                availableSlots,
                occupiedSlots,
                reservedSlots,
                occupancyPercentage
        );
    }

    @Transactional(readOnly = true)
    public List<ZoneStatsDto> getZoneStats() {
        List<ZoneStatsDto> result = new ArrayList<>(ZONES.size());

        for (String zone : ZONES) {
            long totalSlots = parkingSlotRepository.countByZoneIgnoreCase(zone);
            long availableSlots = parkingSlotRepository.countByZoneIgnoreCaseAndStatus(zone, SlotStatus.AVAILABLE);
            long occupiedSlots = parkingSlotRepository.countByZoneIgnoreCaseAndStatus(zone, SlotStatus.OCCUPIED);
            long reservedSlots = parkingSlotRepository.countByZoneIgnoreCaseAndStatus(zone, SlotStatus.RESERVED);

            double occupancyPercentage = calculateOccupancyPercentage(occupiedSlots, totalSlots);

            result.add(new ZoneStatsDto(
                    zone,
                    totalSlots,
                    availableSlots,
                    occupiedSlots,
                    reservedSlots,
                    occupancyPercentage
            ));
        }

        return result;
    }

    private double calculateOccupancyPercentage(long occupiedSlots, long totalSlots) {
        if (totalSlots == 0) {
            return 0.0;
        }
        double percentage = ((double) occupiedSlots / (double) totalSlots) * 100.0;
        return BigDecimal.valueOf(percentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

