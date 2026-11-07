package com.smartparking.controller;

import com.smartparking.dto.SensorReadingRequestDto;
import com.smartparking.dto.SensorReadingResponseDto;
import com.smartparking.service.SensorProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller processing IoT ultrasonic sensor readings.
 */
@RestController
@RequestMapping("/api/sensors")
@Tag(name = "IoT Sensors", description = "Endpoints for IoT ultrasonic sensor reading ingestion and simulator events")
public class SensorReadingController {

    private final SensorProcessingService sensorProcessingService;

    public SensorReadingController(SensorProcessingService sensorProcessingService) {
        this.sensorProcessingService = sensorProcessingService;
    }

    @PostMapping("/readings")
    @Operation(
            summary = "Submit IoT sensor reading",
            description = "Receives an ultrasonic distance measurement from a virtual or physical sensor. "
                    + "Evaluates against the 15 cm threshold to detect vehicle entry or exit, updates PostgreSQL, "
                    + "records state-change events, and returns the resulting parking slot status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor reading processed successfully",
                    content = @Content(schema = @Schema(implementation = SensorReadingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid sensor ID mapping, negative distance, or missing payload",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Target parking slot not found",
                    content = @Content)
    })
    public ResponseEntity<SensorReadingResponseDto> submitSensorReading(
            @Valid @RequestBody SensorReadingRequestDto request) {
        SensorReadingResponseDto response = sensorProcessingService.processSensorReading(request);
        return ResponseEntity.ok(response);
    }
}

