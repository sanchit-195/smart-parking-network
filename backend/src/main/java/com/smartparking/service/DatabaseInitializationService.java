package com.smartparking.service;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.repository.ParkingSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for initializing the 24 parking slots upon application startup.
 * Ensures idempotent behavior so that restarting the application does not duplicate slots
 * or overwrite existing state.
 */
@Service
public class DatabaseInitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationService.class);

    private final ParkingSlotRepository parkingSlotRepository;

    public DatabaseInitializationService(ParkingSlotRepository parkingSlotRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initializeSlotsIfEmpty();
    }

    public synchronized void initializeSlotsIfEmpty() {
        long currentCount = parkingSlotRepository.count();
        if (currentCount > 0) {
            log.info("Database already contains {} parking slots. Skipping initialization.", currentCount);
            return;
        }

        log.info("Initializing 24 parking slots across Zones A, B, and C...");
        List<ParkingSlot> slots = new ArrayList<>(24);
        LocalDateTime now = LocalDateTime.now();

        // Zone A: P01 - P08 (Sensors S01 - S08)
        for (int i = 1; i <= 8; i++) {
            String slotNumber = String.format("P%02d", i);
            String sensorId = String.format("S%02d", i);
            slots.add(createSlot(slotNumber, "A", sensorId, now));
        }

        // Zone B: P09 - P16 (Sensors S09 - S16)
        for (int i = 9; i <= 16; i++) {
            String slotNumber = String.format("P%02d", i);
            String sensorId = String.format("S%02d", i);
            slots.add(createSlot(slotNumber, "B", sensorId, now));
        }

        // Zone C: P17 - P24 (Sensors S17 - S24)
        for (int i = 17; i <= 24; i++) {
            String slotNumber = String.format("P%02d", i);
            String sensorId = String.format("S%02d", i);
            slots.add(createSlot(slotNumber, "C", sensorId, now));
        }

        parkingSlotRepository.saveAll(slots);
        log.info("Successfully initialized 24 parking slots (8 per zone).");
    }

    private ParkingSlot createSlot(String slotNumber, String zone, String sensorId, LocalDateTime now) {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(slotNumber);
        slot.setZone(zone);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setSensorId(sensorId);
        slot.setLastDistanceCm(null);
        slot.setLastSensorUpdate(null);
        slot.setCreatedAt(now);
        slot.setUpdatedAt(now);
        return slot;
    }
}

