package com.smartparking.controller;

import com.smartparking.dto.OverallParkingStatsDto;
import com.smartparking.dto.ParkingEventResponseDto;
import com.smartparking.dto.ParkingSlotResponseDto;
import com.smartparking.dto.ZoneStatsDto;
import com.smartparking.service.ParkingEventService;
import com.smartparking.service.ParkingSlotService;
import com.smartparking.service.ParkingStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller handling parking slot operations, zone filtering, slot reservations,
 * manual releases, statistics, and event history.
 */
@RestController
@RequestMapping("/api/parking")
@Tag(name = "Parking Management", description = "Operations on parking slots, reservations, statistics, and activity history")
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;
    private final ParkingStatsService parkingStatsService;
    private final ParkingEventService parkingEventService;

    public ParkingSlotController(ParkingSlotService parkingSlotService,
                                 ParkingStatsService parkingStatsService,
                                 ParkingEventService parkingEventService) {
        this.parkingSlotService = parkingSlotService;
        this.parkingStatsService = parkingStatsService;
        this.parkingEventService = parkingEventService;
    }

    @GetMapping("/slots")
    @Operation(summary = "Get parking slots", description = "Retrieve all 24 parking slots or filter slots by zone (A, B, C)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of parking slots"),
            @ApiResponse(responseCode = "400", description = "Invalid zone filter parameter supplied", content = @Content)
    })
    public ResponseEntity<List<ParkingSlotResponseDto>> getSlots(
            @Parameter(description = "Optional zone filter ('A', 'B', or 'C')")
            @RequestParam(required = false) String zone) {
        List<ParkingSlotResponseDto> slots = (zone != null && !zone.isBlank())
                ? parkingSlotService.getSlotsByZone(zone)
                : parkingSlotService.getAllSlots();
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/slots/{slotNumber}")
    @Operation(summary = "Get single parking slot", description = "Retrieve details of a specific parking slot by slot number (e.g. P05)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved slot details"),
            @ApiResponse(responseCode = "404", description = "Parking slot not found", content = @Content)
    })
    public ResponseEntity<ParkingSlotResponseDto> getSlotByNumber(
            @Parameter(description = "Slot number, e.g. P05", required = true)
            @PathVariable String slotNumber) {
        return ResponseEntity.ok(parkingSlotService.getSlotByNumber(slotNumber));
    }

    @PostMapping("/slots/{slotNumber}/reserve")
    @Operation(summary = "Reserve parking slot", description = "Reserve an available parking slot (AVAILABLE -> RESERVED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition (slot not AVAILABLE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Slot not found", content = @Content)
    })
    public ResponseEntity<ParkingSlotResponseDto> reserveSlot(
            @Parameter(description = "Slot number, e.g. P05", required = true)
            @PathVariable String slotNumber) {
        return ResponseEntity.ok(parkingSlotService.reserveSlot(slotNumber));
    }

    @PostMapping("/slots/{slotNumber}/cancel-reservation")
    @Operation(summary = "Cancel reservation", description = "Cancel an existing reservation for a parking slot (RESERVED -> AVAILABLE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition (slot not RESERVED)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Slot not found", content = @Content)
    })
    public ResponseEntity<ParkingSlotResponseDto> cancelReservation(
            @Parameter(description = "Slot number, e.g. P05", required = true)
            @PathVariable String slotNumber) {
        return ResponseEntity.ok(parkingSlotService.cancelReservation(slotNumber));
    }

    @PostMapping("/slots/{slotNumber}/release")
    @Operation(summary = "Release occupied slot", description = "Manually release an occupied parking slot (OCCUPIED -> AVAILABLE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition (slot not OCCUPIED)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Slot not found", content = @Content)
    })
    public ResponseEntity<ParkingSlotResponseDto> releaseSlot(
            @Parameter(description = "Slot number, e.g. P05", required = true)
            @PathVariable String slotNumber) {
        return ResponseEntity.ok(parkingSlotService.releaseSlot(slotNumber));
    }

    @PostMapping("/slots/{slotNumber}/occupy")
    @Operation(summary = "Manually occupy slot", description = "Manually occupy an available or reserved parking slot (AVAILABLE/RESERVED -> OCCUPIED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot occupied successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition (slot already OCCUPIED)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Slot not found", content = @Content)
    })
    public ResponseEntity<ParkingSlotResponseDto> occupySlot(
            @Parameter(description = "Slot number, e.g. P05", required = true)
            @PathVariable String slotNumber) {
        return ResponseEntity.ok(parkingSlotService.occupySlot(slotNumber));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overall parking statistics", description = "Calculate real-time facility parking statistics from PostgreSQL")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved parking statistics",
            content = @Content(schema = @Schema(implementation = OverallParkingStatsDto.class)))
    public ResponseEntity<OverallParkingStatsDto> getOverallStats() {
        return ResponseEntity.ok(parkingStatsService.getOverallStats());
    }

    @GetMapping("/zones")
    @Operation(summary = "Get zone-wise statistics", description = "Calculate slot counts and occupancy percentages for Zones A, B, and C")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved zone statistics")
    public ResponseEntity<List<ZoneStatsDto>> getZoneStats() {
        return ResponseEntity.ok(parkingStatsService.getZoneStats());
    }

    @GetMapping("/events")
    @Operation(summary = "Get recent parking activity events", description = "Retrieve latest parking events, newest first")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved event history")
    public ResponseEntity<List<ParkingEventResponseDto>> getRecentEvents(
            @Parameter(description = "Maximum number of events to return (default 20, max 100)")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(parkingEventService.getRecentEvents(limit));
    }
}

