package com.smartparking.repository;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findBySlotNumberIgnoreCase(String slotNumber);

    Optional<ParkingSlot> findBySensorIdIgnoreCase(String sensorId);

    List<ParkingSlot> findAllByOrderBySlotNumberAsc();

    List<ParkingSlot> findByZoneIgnoreCaseOrderBySlotNumberAsc(String zone);

    long countByZoneIgnoreCase(String zone);

    long countByStatus(SlotStatus status);

    long countByZoneIgnoreCaseAndStatus(String zone, SlotStatus status);
}

