package com.smartparking.repository;

import com.smartparking.entity.ParkingEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingEventRepository extends JpaRepository<ParkingEvent, Long> {

    List<ParkingEvent> findAllByOrderByTimestampDesc(Pageable pageable);

    List<ParkingEvent> findBySlotNumberIgnoreCaseOrderByTimestampDesc(String slotNumber, Pageable pageable);
}

