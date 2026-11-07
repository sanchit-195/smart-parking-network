package com.smartparking.service;

import com.smartparking.dto.ParkingSlotResponseDto;
import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.InvalidStateTransitionException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing parking slot retrieval and manual state transitions.
 */
@Service
public class ParkingSlotService {

    private static final Logger log = LoggerFactory.getLogger(ParkingSlotService.class);
    private static final Set<String> VALID_ZONES = Set.of("A", "B", "C");

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingEventService parkingEventService;

    public ParkingSlotService(ParkingSlotRepository parkingSlotRepository,
                              ParkingEventService parkingEventService) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingEventService = parkingEventService;
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotResponseDto> getAllSlots() {
        return parkingSlotRepository.findAllByOrderBySlotNumberAsc()
                .stream()
                .map(ParkingSlotResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSlotResponseDto> getSlotsByZone(String zone) {
        if (zone == null || zone.trim().isEmpty()) {
            return getAllSlots();
        }

        String normalizedZone = zone.trim().toUpperCase();
        if (!VALID_ZONES.contains(normalizedZone)) {
            throw new BadRequestException("Invalid zone: '" + zone + "'. Allowed zones are A, B, or C.");
        }

        return parkingSlotRepository.findByZoneIgnoreCaseOrderBySlotNumberAsc(normalizedZone)
                .stream()
                .map(ParkingSlotResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingSlotResponseDto getSlotByNumber(String slotNumber) {
        ParkingSlot slot = findSlotOrThrow(slotNumber);
        return ParkingSlotResponseDto.fromEntity(slot);
    }

    @Transactional
    public ParkingSlotResponseDto reserveSlot(String slotNumber) {
        ParkingSlot slot = findSlotOrThrow(slotNumber);

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot reserve slot %s: current status is %s. Only AVAILABLE slots can be reserved.",
                            slot.getSlotNumber(), slot.getStatus()));
        }

        SlotStatus previousStatus = slot.getStatus();
        slot.setStatus(SlotStatus.RESERVED);
        slot.setUpdatedAt(LocalDateTime.now());
        ParkingSlot saved = parkingSlotRepository.save(slot);

        parkingEventService.recordEvent(
                saved.getSlotNumber(),
                EventType.SLOT_RESERVED,
                previousStatus,
                SlotStatus.RESERVED,
                saved.getSensorId(),
                saved.getLastDistanceCm(),
                String.format("Slot %s reserved.", saved.getSlotNumber())
        );

        return ParkingSlotResponseDto.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotResponseDto cancelReservation(String slotNumber) {
        ParkingSlot slot = findSlotOrThrow(slotNumber);

        if (slot.getStatus() != SlotStatus.RESERVED) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot cancel reservation for slot %s: current status is %s. Only RESERVED slots can be cancelled.",
                            slot.getSlotNumber(), slot.getStatus()));
        }

        SlotStatus previousStatus = slot.getStatus();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());
        ParkingSlot saved = parkingSlotRepository.save(slot);

        parkingEventService.recordEvent(
                saved.getSlotNumber(),
                EventType.RESERVATION_CANCELLED,
                previousStatus,
                SlotStatus.AVAILABLE,
                saved.getSensorId(),
                saved.getLastDistanceCm(),
                String.format("Reservation cancelled for slot %s.", saved.getSlotNumber())
        );

        return ParkingSlotResponseDto.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotResponseDto releaseSlot(String slotNumber) {
        ParkingSlot slot = findSlotOrThrow(slotNumber);

        if (slot.getStatus() != SlotStatus.OCCUPIED) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot release slot %s: current status is %s. Only OCCUPIED slots can be released.",
                            slot.getSlotNumber(), slot.getStatus()));
        }

        SlotStatus previousStatus = slot.getStatus();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());
        ParkingSlot saved = parkingSlotRepository.save(slot);

        parkingEventService.recordEvent(
                saved.getSlotNumber(),
                EventType.VEHICLE_EXIT,
                previousStatus,
                SlotStatus.AVAILABLE,
                saved.getSensorId(),
                saved.getLastDistanceCm(),
                String.format("Slot %s manually released. Marked as AVAILABLE.", saved.getSlotNumber())
        );

        return ParkingSlotResponseDto.fromEntity(saved);
    }

    @Transactional
    public ParkingSlotResponseDto occupySlot(String slotNumber) {
        ParkingSlot slot = findSlotOrThrow(slotNumber);

        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new InvalidStateTransitionException(
                    String.format("Slot %s is already OCCUPIED.", slot.getSlotNumber()));
        }

        SlotStatus previousStatus = slot.getStatus();
        slot.setStatus(SlotStatus.OCCUPIED);
        slot.setUpdatedAt(LocalDateTime.now());
        ParkingSlot saved = parkingSlotRepository.save(slot);

        parkingEventService.recordEvent(
                saved.getSlotNumber(),
                EventType.VEHICLE_ENTRY,
                previousStatus,
                SlotStatus.OCCUPIED,
                saved.getSensorId(),
                saved.getLastDistanceCm(),
                String.format("Slot %s manually marked as OCCUPIED.", saved.getSlotNumber())
        );

        return ParkingSlotResponseDto.fromEntity(saved);
    }

    public ParkingSlot findSlotOrThrow(String slotNumber) {
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            throw new BadRequestException("Slot number must not be empty.");
        }
        return parkingSlotRepository.findBySlotNumberIgnoreCase(slotNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with slot number: " + slotNumber));
    }
}

