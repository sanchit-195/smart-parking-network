package com.smartparking.service;

import com.smartparking.dto.ParkingEventResponseDto;
import com.smartparking.entity.EventType;
import com.smartparking.entity.ParkingEvent;
import com.smartparking.entity.SlotStatus;
import com.smartparking.repository.ParkingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing parking activity audit events.
 */
@Service
public class ParkingEventService {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventService.class);

    private final ParkingEventRepository parkingEventRepository;

    public ParkingEventService(ParkingEventRepository parkingEventRepository) {
        this.parkingEventRepository = parkingEventRepository;
    }

    @Transactional
    public ParkingEvent recordEvent(String slotNumber, EventType eventType, SlotStatus previousStatus,
                                     SlotStatus newStatus, String sensorId, Double distanceCm,
                                     String description) {
        ParkingEvent event = new ParkingEvent();
        event.setSlotNumber(slotNumber);
        event.setEventType(eventType);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setSensorId(sensorId);
        event.setDistanceCm(distanceCm);
        event.setDescription(description);
        event.setTimestamp(LocalDateTime.now());

        ParkingEvent saved = parkingEventRepository.save(event);
        log.info("Recorded event: [{}] Slot {} transitioned from {} to {}. Description: {}",
                eventType, slotNumber, previousStatus, newStatus, description);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ParkingEventResponseDto> getRecentEvents(int limit) {
        int sanitizedLimit = Math.max(1, Math.min(limit, 100));
        Pageable pageable = PageRequest.of(0, sanitizedLimit);
        return parkingEventRepository.findAllByOrderByTimestampDesc(pageable)
                .stream()
                .map(ParkingEventResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}

