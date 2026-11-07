# Smart Parking Network — REST API Documentation

This document provides complete technical specifications and integration guidelines for the **REST API-Driven Dynamic Smart Parking Network** Spring Boot backend. It is designed for frontend developers and external API consumers to integrate with the backend without needing access to the backend source code.

---

## 1. Base URL & Environment Setup

### Base URL
- Local Backend URL: `http://localhost:8080`
- Swagger UI (Interactive Testing): `http://localhost:8080/swagger-ui/index.html`
- OpenAPI Specification (JSON): `http://localhost:8080/v3/api-docs`

### Frontend Configuration (`.env`)
For Vite + React:
```env
VITE_API_BASE_URL=http://localhost:8080
```

### CORS Configuration
The backend allows cross-origin requests from the following origins:
- `http://localhost:5173` (Vite dev server default)
- `http://localhost:3000` (Create React App default)

Supported HTTP Methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`  
Supported Headers: `*`  
Credentials: Supported (`Access-Control-Allow-Credentials: true`)

---

## 2. Core Domain Concepts

### 2.1 Parking Slots & Zones
The facility manages **24 parking slots** divided into 3 zones (8 slots per zone):
- **Zone A:** `P01` to `P08`
- **Zone B:** `P09` to `P16`
- **Zone C:** `P17` to `P24`

### 2.2 Virtual Sensor Mappings
Each slot is mapped 1:1 to a unique virtual ultrasonic IoT sensor:
- `P01` ↔ `S01`
- `P02` ↔ `S02`
- ...
- `P24` ↔ `S24`

### 2.3 Slot Statuses
| Status | Description | UI Color Recommendation |
|---|---|---|
| `AVAILABLE` | The slot is empty and ready for parking or reservation. | Green (`#22c55e` / `bg-green-500`) |
| `OCCUPIED` | A vehicle is actively parked in the slot. | Red (`#ef4444` / `bg-red-500`) |
| `RESERVED` | The slot is reserved for a future vehicle arrival. | Yellow/Amber (`#f59e0b` / `bg-amber-500`) |

### 2.4 Sensor Detection Rule
The system uses a configurable threshold of **15.0 cm**:
- **Distance < 15.0 cm:** Vehicle detected.
- **Distance >= 15.0 cm:** No vehicle detected.

### 2.5 State Transition Rules
| Current State | Condition / Action | Resulting State | Event Generated |
|---|---|---|---|
| `AVAILABLE` | Sensor reports distance < 15 cm | `OCCUPIED` | `VEHICLE_ENTRY` |
| `AVAILABLE` | Sensor reports distance >= 15 cm | `AVAILABLE` | *None* |
| `AVAILABLE` | User reserves slot | `RESERVED` | `SLOT_RESERVED` |
| `AVAILABLE` | User manually occupies slot | `OCCUPIED` | `VEHICLE_ENTRY` |
| `OCCUPIED` | Sensor reports distance >= 15 cm | `AVAILABLE` | `VEHICLE_EXIT` |
| `OCCUPIED` | Sensor reports distance < 15 cm | `OCCUPIED` | *None* |
| `OCCUPIED` | User manually releases slot | `AVAILABLE` | `VEHICLE_EXIT` |
| `RESERVED` | Sensor reports distance < 15 cm | `OCCUPIED` | `VEHICLE_ENTRY` |
| `RESERVED` | Sensor reports distance >= 15 cm | `RESERVED` | *None* |
| `RESERVED` | User cancels reservation | `AVAILABLE` | `RESERVATION_CANCELLED` |

*Note: Repeated sensor readings that do not trigger a state transition update the slot's distance reading and timestamp, but do NOT produce duplicate history events.*

---

## 3. Endpoints Overview

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/parking/slots` | Retrieve all 24 slots (or filter by zone) |
| `GET` | `/api/parking/slots/{slotNumber}` | Retrieve details for a specific slot |
| `POST` | `/api/sensors/readings` | Submit simulated IoT sensor reading |
| `POST` | `/api/parking/slots/{slotNumber}/reserve` | Reserve an available parking slot |
| `POST` | `/api/parking/slots/{slotNumber}/cancel-reservation` | Cancel an existing reservation |
| `POST` | `/api/parking/slots/{slotNumber}/release` | Manually release an occupied slot |
| `POST` | `/api/parking/slots/{slotNumber}/occupy` | Manually occupy an available slot |
| `GET` | `/api/parking/stats` | Retrieve overall facility statistics |
| `GET` | `/api/parking/zones` | Retrieve zone-wise statistics (Zones A, B, C) |
| `GET` | `/api/parking/events` | Retrieve recent activity events log |

---

## 4. Detailed API Specifications

### 4.1 Get All Slots (or Filter by Zone)
`GET /api/parking/slots`

**Query Parameters:**
- `zone` *(optional, string)*: Filter by zone letter (`A`, `B`, or `C`). If omitted, returns all 24 slots.

**Example Request:**
```http
GET /api/parking/slots?zone=A HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "slotNumber": "P01",
    "zone": "A",
    "status": "AVAILABLE",
    "sensorId": "S01",
    "lastDistanceCm": null,
    "lastSensorUpdate": null,
    "createdAt": "2026-11-07T16:50:00",
    "updatedAt": "2026-11-07T16:50:00"
  },
  {
    "id": 2,
    "slotNumber": "P02",
    "zone": "A",
    "status": "OCCUPIED",
    "sensorId": "S02",
    "lastDistanceCm": 7.4,
    "lastSensorUpdate": "2026-11-07T16:55:10",
    "createdAt": "2026-11-07T16:50:00",
    "updatedAt": "2026-11-07T16:55:10"
  }
]
```

---

### 4.2 Get Single Slot Details
`GET /api/parking/slots/{slotNumber}`

**Path Parameters:**
- `slotNumber` *(required, string)*: The slot identifier, e.g., `P05`.

**Example Request:**
```http
GET /api/parking/slots/P05 HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
{
  "id": 5,
  "slotNumber": "P05",
  "zone": "A",
  "status": "AVAILABLE",
  "sensorId": "S05",
  "lastDistanceCm": 32.5,
  "lastSensorUpdate": "2026-11-07T16:52:00",
  "createdAt": "2026-11-07T16:50:00",
  "updatedAt": "2026-11-07T16:52:00"
}
```

**Response `404 Not Found`:**
```json
{
  "timestamp": "2026-11-07T16:53:10",
  "status": 404,
  "error": "Not Found",
  "message": "Parking slot not found with slot number: P99",
  "path": "/api/parking/slots/P99"
}
```

---

### 4.3 Submit IoT Sensor Reading (Simulator Endpoint)
`POST /api/sensors/readings`

Receives ultrasonic sensor readings from the IoT simulator. Evaluates the reading against the 15 cm threshold, updates the database, generates audit events when status changes, and returns the updated status.

**Headers:**
```http
Content-Type: application/json
```

**Request Body:**
```json
{
  "sensorId": "S05",
  "slotId": "P05",
  "distanceCm": 8.2
}
```

| Field | Type | Validation | Description |
|---|---|---|---|
| `sensorId` | string | Required, not blank | Sensor ID, e.g. `S05` |
| `slotId` | string | Required, not blank | Slot number, e.g. `P05` |
| `distanceCm` | number | Required, >= 0.0 | Measured distance in cm |

**Response `200 OK` (Vehicle Entry):**
```json
{
  "sensorId": "S05",
  "slotId": "P05",
  "distanceCm": 8.2,
  "vehicleDetected": true,
  "slotStatus": "OCCUPIED",
  "message": "Vehicle detected. Parking slot marked as occupied."
}
```

**Response `200 OK` (Vehicle Exit):**
```json
{
  "sensorId": "S05",
  "slotId": "P05",
  "distanceCm": 30.0,
  "vehicleDetected": false,
  "slotStatus": "AVAILABLE",
  "message": "Vehicle left. Parking slot marked as available."
}
```

**Response `400 Bad Request` (Mismatched Sensor):**
```json
{
  "timestamp": "2026-11-07T16:54:00",
  "status": 400,
  "error": "Invalid Sensor Mapping",
  "message": "Sensor S08 is not mapped to slot P05. Expected sensor: S05",
  "path": "/api/sensors/readings"
}
```

---

### 4.4 Reserve Slot
`POST /api/parking/slots/{slotNumber}/reserve`

Reserves an available parking slot (`AVAILABLE` → `RESERVED`).

**Example Request:**
```http
POST /api/parking/slots/P07/reserve HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
{
  "id": 7,
  "slotNumber": "P07",
  "zone": "A",
  "status": "RESERVED",
  "sensorId": "S07",
  "lastDistanceCm": null,
  "lastSensorUpdate": null,
  "createdAt": "2026-11-07T16:50:00",
  "updatedAt": "2026-11-07T16:58:30"
}
```

**Response `400 Bad Request` (Slot not available):**
```json
{
  "timestamp": "2026-11-07T16:59:00",
  "status": 400,
  "error": "Invalid State Transition",
  "message": "Cannot reserve slot P07: current status is OCCUPIED. Only AVAILABLE slots can be reserved.",
  "path": "/api/parking/slots/P07/reserve"
}
```

---

### 4.5 Cancel Reservation
`POST /api/parking/slots/{slotNumber}/cancel-reservation`

Cancels an active reservation (`RESERVED` → `AVAILABLE`).

**Example Request:**
```http
POST /api/parking/slots/P07/cancel-reservation HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
{
  "id": 7,
  "slotNumber": "P07",
  "zone": "A",
  "status": "AVAILABLE",
  "sensorId": "S07",
  "lastDistanceCm": null,
  "lastSensorUpdate": null,
  "createdAt": "2026-11-07T16:50:00",
  "updatedAt": "2026-11-07T17:00:15"
}
```

---

### 4.6 Manually Release Slot
`POST /api/parking/slots/{slotNumber}/release`

Releases an occupied slot (`OCCUPIED` → `AVAILABLE`).

**Example Request:**
```http
POST /api/parking/slots/P05/release HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
{
  "id": 5,
  "slotNumber": "P05",
  "zone": "A",
  "status": "AVAILABLE",
  "sensorId": "S05",
  "lastDistanceCm": 8.2,
  "lastSensorUpdate": "2026-11-07T16:52:00",
  "createdAt": "2026-11-07T16:50:00",
  "updatedAt": "2026-11-07T17:01:00"
}
```

---

### 4.7 Manually Occupy Slot
`POST /api/parking/slots/{slotNumber}/occupy`

Manually marks an available or reserved slot as occupied (`AVAILABLE`/`RESERVED` → `OCCUPIED`).

**Example Request:**
```http
POST /api/parking/slots/P01/occupy HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "slotNumber": "P01",
  "zone": "A",
  "status": "OCCUPIED",
  "sensorId": "S01",
  "lastDistanceCm": null,
  "lastSensorUpdate": null,
  "createdAt": "2026-11-07T16:50:00",
  "updatedAt": "2026-11-07T17:01:30"
}
```

---

### 4.8 Get Overall Parking Statistics
`GET /api/parking/stats`

Calculates live capacity and occupancy metrics directly from the PostgreSQL database.  
**Formula:** `occupancyPercentage = (occupiedSlots / totalSlots) * 100` (rounded to 2 decimal places).  
*Reserved slots are NOT counted as occupied.*

**Response `200 OK`:**
```json
{
  "totalSlots": 24,
  "availableSlots": 11,
  "occupiedSlots": 10,
  "reservedSlots": 3,
  "occupancyPercentage": 41.67
}
```

---

### 4.9 Get Zone-Wise Statistics
`GET /api/parking/zones`

Returns breakdown metrics for Zones A, B, and C.

**Response `200 OK`:**
```json
[
  {
    "zone": "A",
    "totalSlots": 8,
    "availableSlots": 5,
    "occupiedSlots": 2,
    "reservedSlots": 1,
    "occupancyPercentage": 25.0
  },
  {
    "zone": "B",
    "totalSlots": 8,
    "availableSlots": 4,
    "occupiedSlots": 4,
    "reservedSlots": 0,
    "occupancyPercentage": 50.0
  },
  {
    "zone": "C",
    "totalSlots": 8,
    "availableSlots": 2,
    "occupiedSlots": 4,
    "reservedSlots": 2,
    "occupancyPercentage": 50.0
  }
]
```

---

### 4.10 Get Recent Activity Events
`GET /api/parking/events`

Returns latest activity logs in reverse chronological order (newest first).

**Query Parameters:**
- `limit` *(optional, integer, default: 20, max: 100)*: Number of events to return.

**Example Request:**
```http
GET /api/parking/events?limit=5 HTTP/1.1
Host: localhost:8080
```

**Response `200 OK`:**
```json
[
  {
    "id": 14,
    "slotNumber": "P05",
    "eventType": "VEHICLE_ENTRY",
    "previousStatus": "AVAILABLE",
    "newStatus": "OCCUPIED",
    "sensorId": "S05",
    "distanceCm": 8.2,
    "description": "Vehicle detected. Slot marked OCCUPIED.",
    "timestamp": "2026-11-07T17:02:10"
  },
  {
    "id": 13,
    "slotNumber": "P08",
    "eventType": "SLOT_RESERVED",
    "previousStatus": "AVAILABLE",
    "newStatus": "RESERVED",
    "sensorId": "S08",
    "distanceCm": null,
    "description": "Slot P08 reserved.",
    "timestamp": "2026-11-07T17:01:25"
  }
]
```

---

## 5. React Integration Guide

Here is a recommended setup for the React frontend teammate to connect seamlessly to the Spring Boot backend.

### 5.1 API Client (`src/services/api.js`)
```javascript
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  return response.json();
}

export const parkingApi = {
  // Slots
  getAllSlots: (zone) => request(zone ? `/api/parking/slots?zone=${zone}` : '/api/parking/slots'),
  getSlotByNumber: (slotNumber) => request(`/api/parking/slots/${slotNumber}`),
  
  // Slot Actions
  reserveSlot: (slotNumber) => request(`/api/parking/slots/${slotNumber}/reserve`, { method: 'POST' }),
  cancelReservation: (slotNumber) => request(`/api/parking/slots/${slotNumber}/cancel-reservation`, { method: 'POST' }),
  releaseSlot: (slotNumber) => request(`/api/parking/slots/${slotNumber}/release`, { method: 'POST' }),
  occupySlot: (slotNumber) => request(`/api/parking/slots/${slotNumber}/occupy`, { method: 'POST' }),

  // Stats & Events
  getStats: () => request('/api/parking/stats'),
  getZoneStats: () => request('/api/parking/zones'),
  getRecentEvents: (limit = 20) => request(`/api/parking/events?limit=${limit}`),

  // IoT Sensor Simulator
  sendSensorReading: (sensorId, slotId, distanceCm) =>
    request('/api/sensors/readings', {
      method: 'POST',
      body: JSON.stringify({ sensorId, slotId, distanceCm }),
    }),
};
```

### 5.2 Polling Pattern Example (`src/hooks/useParkingDashboard.js`)
```javascript
import { useState, useEffect, useCallback } from 'react';
import { parkingApi } from '../services/api';

export function useParkingDashboard(pollIntervalMs = 4000) {
  const [slots, setSlots] = useState([]);
  const [stats, setStats] = useState(null);
  const [zoneStats, setZoneStats] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refreshData = useCallback(async () => {
    try {
      const [slotsData, statsData, zoneData, eventsData] = await Promise.all([
        parkingApi.getAllSlots(),
        parkingApi.getStats(),
        parkingApi.getZoneStats(),
        parkingApi.getRecentEvents(10),
      ]);
      setSlots(slotsData);
      setStats(statsData);
      setZoneStats(zoneData);
      setEvents(eventsData);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshData();
    const interval = setInterval(refreshData, pollIntervalMs);
    return () => clearInterval(interval);
  }, [refreshData, pollIntervalMs]);

  return { slots, stats, zoneStats, events, loading, error, refreshData };
}
```

### 5.3 Simulator Shortcuts in Frontend
- **Simulate Vehicle Entry:**
  ```javascript
  // Sends 8.0 cm (below 15 cm threshold)
  await parkingApi.sendSensorReading(selectedSensorId, selectedSlotId, 8.0);
  await refreshData();
  ```
- **Simulate Vehicle Exit:**
  ```javascript
  // Sends 30.0 cm (above 15 cm threshold)
  await parkingApi.sendSensorReading(selectedSensorId, selectedSlotId, 30.0);
  await refreshData();
  ```

---

## 6. cURL Request Examples

```bash
# 1. Get all slots
curl -X GET http://localhost:8080/api/parking/slots

# 2. Filter Zone B slots
curl -X GET "http://localhost:8080/api/parking/slots?zone=B"

# 3. Simulate vehicle entry on P05 (8.2 cm)
curl -X POST http://localhost:8080/api/sensors/readings \
  -H "Content-Type: application/json" \
  -d '{"sensorId":"S05","slotId":"P05","distanceCm":8.2}'

# 4. Simulate vehicle exit on P05 (30 cm)
curl -X POST http://localhost:8080/api/sensors/readings \
  -H "Content-Type: application/json" \
  -d '{"sensorId":"S05","slotId":"P05","distanceCm":30.0}'

# 5. Reserve slot P12
curl -X POST http://localhost:8080/api/parking/slots/P12/reserve

# 6. Cancel reservation for P12
curl -X POST http://localhost:8080/api/parking/slots/P12/cancel-reservation

# 7. Get parking stats
curl -X GET http://localhost:8080/api/parking/stats

# 8. Get zone stats
curl -X GET http://localhost:8080/api/parking/zones

# 9. Get recent events (limit 10)
curl -X GET "http://localhost:8080/api/parking/events?limit=10"
```

