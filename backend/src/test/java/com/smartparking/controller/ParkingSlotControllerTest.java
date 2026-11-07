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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ParkingSlotControllerTest {

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
    @DisplayName("GET /api/parking/slots returns all 24 slots")
    void testGetAllSlots() throws Exception {
        mockMvc.perform(get("/api/parking/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(24)))
                .andExpect(jsonPath("$[0].slotNumber", is("P01")))
                .andExpect(jsonPath("$[23].slotNumber", is("P24")));
    }

    @Test
    @DisplayName("GET /api/parking/slots?zone=A returns 8 slots for Zone A")
    void testGetSlotsByZone() throws Exception {
        mockMvc.perform(get("/api/parking/slots").param("zone", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].zone", is("A")));
    }

    @Test
    @DisplayName("GET /api/parking/slots?zone=INVALID returns 400 Bad Request")
    void testGetSlotsByInvalidZone() throws Exception {
        mockMvc.perform(get("/api/parking/slots").param("zone", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/parking/slots/{slotNumber} returns slot details or 404")
    void testGetSlotByNumber() throws Exception {
        mockMvc.perform(get("/api/parking/slots/P05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber", is("P05")))
                .andExpect(jsonPath("$.sensorId", is("S05")))
                .andExpect(jsonPath("$.zone", is("A")));

        mockMvc.perform(get("/api/parking/slots/P99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/parking/slots/{slotNumber}/reserve reserves available slot")
    void testReserveSlotEndpoint() throws Exception {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P07").orElseThrow();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        mockMvc.perform(post("/api/parking/slots/P07/reserve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber", is("P07")))
                .andExpect(jsonPath("$.status", is("RESERVED")));

        // Attempting to reserve again returns 400
        mockMvc.perform(post("/api/parking/slots/P07/reserve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid State Transition")));
    }

    @Test
    @DisplayName("POST /api/parking/slots/{slotNumber}/cancel-reservation cancels reservation")
    void testCancelReservationEndpoint() throws Exception {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P08").orElseThrow();
        slot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(slot);

        mockMvc.perform(post("/api/parking/slots/P08/cancel-reservation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber", is("P08")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    @Test
    @DisplayName("POST /api/parking/slots/{slotNumber}/release releases occupied slot")
    void testReleaseSlotEndpoint() throws Exception {
        ParkingSlot slot = parkingSlotRepository.findBySlotNumberIgnoreCase("P09").orElseThrow();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        mockMvc.perform(post("/api/parking/slots/P09/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber", is("P09")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    @Test
    @DisplayName("GET /api/parking/stats returns overall stats")
    void testGetStatsEndpoint() throws Exception {
        mockMvc.perform(get("/api/parking/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSlots", is(24)))
                .andExpect(jsonPath("$.occupancyPercentage").isNumber());
    }

    @Test
    @DisplayName("GET /api/parking/zones returns stats for Zones A, B, and C")
    void testGetZoneStatsEndpoint() throws Exception {
        mockMvc.perform(get("/api/parking/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].zone", is("A")))
                .andExpect(jsonPath("$[1].zone", is("B")))
                .andExpect(jsonPath("$[2].zone", is("C")));
    }

    @Test
    @DisplayName("GET /api/parking/events returns list of recent events")
    void testGetRecentEventsEndpoint() throws Exception {
        mockMvc.perform(get("/api/parking/events").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

