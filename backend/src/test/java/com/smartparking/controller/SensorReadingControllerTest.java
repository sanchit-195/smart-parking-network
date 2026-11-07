package com.smartparking.controller;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.SlotStatus;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.service.DatabaseInitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SensorReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private DatabaseInitializationService databaseInitializationService;

    @BeforeEach
    void setUp() {
        databaseInitializationService.initializeSlotsIfEmpty();
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Vehicle detected (< 15 cm) changes AVAILABLE slot to OCCUPIED")
    void testVehicleDetected() throws Exception {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        String json = """
                {
                    "sensorId": "S05",
                    "slotId": "P05",
                    "distanceCm": 8.2
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId", is("S05")))
                .andExpect(jsonPath("$.slotId", is("P05")))
                .andExpect(jsonPath("$.distanceCm", is(8.2)))
                .andExpect(jsonPath("$.vehicleDetected", is(true)))
                .andExpect(jsonPath("$.slotStatus", is("OCCUPIED")))
                .andExpect(jsonPath("$.message", is("Vehicle detected. Parking slot marked as occupied.")));
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Vehicle left (>= 15 cm) changes OCCUPIED slot to AVAILABLE")
    void testVehicleLeft() throws Exception {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P05").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        String json = """
                {
                    "sensorId": "S05",
                    "slotId": "P05",
                    "distanceCm": 30.0
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId", is("S05")))
                .andExpect(jsonPath("$.slotId", is("P05")))
                .andExpect(jsonPath("$.distanceCm", is(30.0)))
                .andExpect(jsonPath("$.vehicleDetected", is(false)))
                .andExpect(jsonPath("$.slotStatus", is("AVAILABLE")))
                .andExpect(jsonPath("$.message", is("Vehicle left. Parking slot marked as available.")));
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Mismatched sensor returns 400 Bad Request")
    void testMismatchedSensorReturns400() throws Exception {
        String json = """
                {
                    "sensorId": "S08",
                    "slotId": "P05",
                    "distanceCm": 8.0
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid Sensor Mapping")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Non-existent slot returns 404 Not Found")
    void testNonExistentSlotReturns404() throws Exception {
        String json = """
                {
                    "sensorId": "S99",
                    "slotId": "P99",
                    "distanceCm": 8.0
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Negative distance returns 400 Bad Request")
    void testNegativeDistanceReturns400() throws Exception {
        String json = """
                {
                    "sensorId": "S05",
                    "slotId": "P05",
                    "distanceCm": -10.0
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/sensors/readings - Missing required fields returns 400 Validation Error")
    void testMissingFieldsReturns400() throws Exception {
        String json = """
                {
                    "distanceCm": 12.0
                }
                """;

        mockMvc.perform(post("/api/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.validationErrors.sensorId").exists())
                .andExpect(jsonPath("$.validationErrors.slotId").exists());
    }
}

