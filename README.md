# REST API-Driven Dynamic Smart Parking Network

A simple, working IoT-based smart parking management prototype for a college lab demonstration.

The project simulates a parking facility with 24 parking slots. A user can view slot availability on a web dashboard and simulate a vehicle entering or leaving a slot. The simulated IoT sensor sends a reading to the Spring Boot backend through a REST API. The backend processes the reading, updates PostgreSQL, and the dashboard displays the latest parking status.

> **Project scope:** This is a hardware-independent IoT prototype. The sensor readings are simulated in the web application. No physical Arduino, ultrasonic sensor, or other hardware is required for the project to work. A physical sensor demonstration can be added separately later, but it is not part of the required implementation.

---

## 1. Main Objective

To demonstrate how an IoT-based parking system can automatically monitor parking slots and update their availability using sensor data and REST API communication.

The project should be simple, stable, easy to understand, and reliable during a live demonstration. Prioritize working functionality over a large number of features.

## 2. Technology Stack

Use only the following core technologies:

| Layer | Technology |
|---|---|
| Frontend | React with Vite |
| Styling | Tailwind CSS |
| Backend | Java 17, Spring Boot 3, Maven |
| API communication | REST APIs with JSON |
| Database | PostgreSQL |
| API documentation/testing | Swagger UI / OpenAPI |
| Frontend icons/charts | A lightweight icon library and simple CSS-based charts, only where useful |

Do **not** add Kafka, Redis, WebSockets, microservices, cloud deployment, authentication, payment gateways, or Docker. They are not required for this college demonstration.

Use a simple monolithic Spring Boot backend with a clean layered structure:

`Controller → Service → Repository → PostgreSQL`

The frontend and backend should be separate applications in one project folder.

## 3. Project Structure

Create the project from scratch using this structure (small variations are acceptable if the separation remains clear):

```text
smart-parking-network/
│
├── frontend/                 # React + Vite + Tailwind CSS
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/         # REST API calls
│   │   ├── hooks/             # Only if needed
│   │   ├── utils/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── .env.example
│   └── package.json
│
├── backend/                  # Spring Boot + Maven
│   ├── src/main/java/.../
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── README.md
└── .gitignore
```

Keep the implementation straightforward. Do not create unnecessary packages or abstraction layers.

## 4. Parking Facility

The simulated parking facility contains **24 slots divided into 3 zones**:

| Zone | Parking slots | Number of slots |
|---|---|---:|
| Zone A | P01–P08 | 8 |
| Zone B | P09–P16 | 8 |
| Zone C | P17–P24 | 8 |
| **Total** | **P01–P24** | **24** |

Each slot has one uniquely mapped virtual sensor:

- P01 → S01, P02 → S02, and so on.
- P24 → S24.

Each parking slot has one of these states:

- `AVAILABLE` — no vehicle is parked in the slot.
- `OCCUPIED` — a vehicle is detected in the slot.
- `RESERVED` — the slot has been reserved and is waiting for the vehicle.

All 24 slots must be created automatically when the backend starts for the first time. Initially, every slot must be `AVAILABLE`. Restarting the backend must not create duplicate slots or reset existing data.

## 5. Core Features

Implement the following features. These are the required project scope; do not expand the scope without a good reason.

### 5.1 Parking Dashboard

Create a polished, responsive dashboard with a clean modern design. It should look professional when shown on a laptop during a lab evaluation.

The dashboard must display:

1. Total parking slots.
2. Available slots.
3. Occupied slots.
4. Reserved slots.
5. Occupancy percentage.
6. A visual parking layout divided into Zone A, Zone B, and Zone C.
7. A recent parking activity/event list.
8. An IoT simulator section.

Use simple, consistent visual status indicators:

- Green: Available.
- Red: Occupied.
- Yellow/amber: Reserved.

Every slot should be visible as a card/tile showing its slot number and status. Clicking a slot should open a small details panel or modal showing its zone, sensor ID, status, last sensor reading, and last updated time.

The dashboard must load its data from the backend. Do not use hardcoded values for the actual dashboard statistics or slot statuses.

### 5.2 Multi-Zone Management

Display three separate sections: Zone A, Zone B, and Zone C. Each zone should show its eight parking slots and a small summary of available, occupied, and reserved slots in that zone.

A zone filter is optional because the three zones are already visible on the main dashboard.

### 5.3 IoT Sensor Simulator

This is the main demonstration feature.

Create a simulator panel in the frontend that allows the user to select a parking slot and simulate an ultrasonic sensor reading. The simulator should include:

- A slot selector (P01–P24).
- The corresponding sensor ID.
- A distance reading in centimetres.
- A slider or input to change the distance.
- A **Send Sensor Reading** button.
- Clear indication of whether a vehicle is detected.

Use this simple sensor rule:

- Distance **below 15 cm** → vehicle detected → slot becomes `OCCUPIED`.
- Distance **15 cm or above** → no vehicle detected.

When the user sends a reading, the frontend must call `POST /api/sensors/readings`. The frontend must not directly change the database or pretend that the backend was updated without a successful API response.

For a smooth demonstration, also provide two convenient actions:

- **Simulate Vehicle Entry:** send a reading such as `8 cm` for the selected slot.
- **Simulate Vehicle Exit:** send a reading such as `30 cm` for the selected slot.

These actions must use the same sensor REST API. They are shortcuts for the simulator, not separate fake status updates.

A visual virtual car or drag-and-drop interaction may be added only if it can be implemented reliably without making the project harder to run. It is optional; the slot selector and sensor controls are sufficient for the required demonstration.

### 5.4 Automatic Dashboard Refresh

After a successful sensor request or manual parking action, refresh the affected slot, statistics, and recent events from the backend.

Also poll the backend every **3–5 seconds** so that the dashboard reflects changes made through another browser tab or API client. Stop the polling when the page is not active if convenient.

Use ordinary REST polling. WebSockets and MQTT are not required.

### 5.5 Parking Slot Actions

Provide simple actions to:

- Reserve an available slot.
- Cancel a reservation.
- Manually release an occupied slot if needed for demonstration/testing.

Do not allow invalid state changes. For example, an occupied slot cannot be reserved, and an already reserved slot cannot be reserved again.

### 5.6 Recent Activity

Show the latest parking events, newest first. Each event should show the slot number, event description/type, and time.

Examples:

- `P05 — Vehicle detected. Slot marked OCCUPIED.`
- `P05 — Vehicle left. Slot marked AVAILABLE.`
- `P12 — Slot reserved.`
- `P12 — Reservation cancelled.`

Repeated sensor readings that do not change the slot state must not create duplicate status-change events.

## 6. Backend Requirements

Use Java 17 and Spring Boot 3 with Maven. Use Spring Web, Spring Data JPA, PostgreSQL Driver, Jakarta Validation, and Springdoc OpenAPI for Swagger documentation.

Use DTOs for API requests and responses. Do not return JPA entities directly from controllers.

### 6.1 Database Entities

Only two main tables are required.

**ParkingSlot**

| Field | Description |
|---|---|
| id | Primary key |
| slotNumber | Unique slot number, e.g. P05 |
| zone | A, B, or C |
| status | AVAILABLE, OCCUPIED, or RESERVED |
| sensorId | Unique sensor ID, e.g. S05 |
| lastDistanceCm | Last received sensor distance; nullable initially |
| lastSensorUpdate | Time of the last sensor reading |
| createdAt | Creation timestamp |
| updatedAt | Last update timestamp |

**ParkingEvent**

| Field | Description |
|---|---|
| id | Primary key |
| slotNumber | Related parking slot number |
| eventType | VEHICLE_ENTRY, VEHICLE_EXIT, SLOT_RESERVED, or RESERVATION_CANCELLED |
| previousStatus | Previous slot status |
| newStatus | New slot status |
| sensorId | Related sensor ID, nullable for manual actions |
| distanceCm | Sensor distance, nullable for manual actions |
| description | Human-readable event description |
| timestamp | Event timestamp |

A simple relationship between `ParkingSlot` and `ParkingEvent` is acceptable. Prefer a design that is easy to understand and maintain.

### 6.2 Sensor Processing Logic

Implement:

`POST /api/sensors/readings`

Example request:

```json
{
  "sensorId": "S05",
  "slotId": "P05",
  "distanceCm": 8.2
}
```

Example response for a vehicle entering an available slot:

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

Processing rules:

1. Validate that the sensor exists and is mapped to the supplied slot.
2. Reject negative distance values and invalid/missing fields.
3. Use a configurable threshold of 15 cm (not a magic number repeated throughout the code).
4. If distance is below the threshold, a vehicle is detected.
5. If distance is at or above the threshold, no vehicle is detected.
6. Update `lastDistanceCm`, `lastSensorUpdate`, and `updatedAt` for every valid reading.
7. Change the slot state and create a `ParkingEvent` only when a valid state transition occurs.
8. If the slot is `RESERVED` and the sensor reports no vehicle, keep it `RESERVED`.
9. If a vehicle is detected in a `RESERVED` slot, change it to `OCCUPIED`.
10. If no vehicle is detected in an `OCCUPIED` slot, change it to `AVAILABLE`.
11. Return the slot's actual resulting status in the response.

Keep the sensor threshold in application configuration, with a default value of 15 cm.

### 6.3 Parking State Rules

Use these state transitions:

| Current state | Action / sensor result | New state |
|---|---|---|
| AVAILABLE | Vehicle detected | OCCUPIED |
| OCCUPIED | No vehicle detected | AVAILABLE |
| AVAILABLE | Reserve slot | RESERVED |
| RESERVED | Vehicle detected | OCCUPIED |
| RESERVED | Cancel reservation | AVAILABLE |
| RESERVED | No vehicle detected | RESERVED |

Reject unsupported transitions with a meaningful error response. A repeated reading that leaves the state unchanged should update the last reading/time but should not create another event.

### 6.4 Required REST APIs

Implement these endpoints with consistent JSON responses.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/parking/slots` | Get all 24 slots |
| GET | `/api/parking/slots/{slotNumber}` | Get one slot, e.g. P05 |
| GET | `/api/parking/slots?zone=A` | Filter slots by zone |
| POST | `/api/sensors/readings` | Submit a simulated IoT sensor reading |
| POST | `/api/parking/slots/{slotNumber}/reserve` | Reserve an available slot |
| POST | `/api/parking/slots/{slotNumber}/cancel-reservation` | Cancel a reservation |
| POST | `/api/parking/slots/{slotNumber}/release` | Manually release an occupied slot |
| GET | `/api/parking/stats` | Get overall parking statistics |
| GET | `/api/parking/zones` | Get zone-wise statistics |
| GET | `/api/parking/events` | Get recent parking events |

For `/api/parking/events`, support an optional `limit` query parameter with a sensible default (for example, 10 or 20).

The statistics response should resemble:

```json
{
  "totalSlots": 24,
  "availableSlots": 11,
  "occupiedSlots": 10,
  "reservedSlots": 3,
  "occupancyPercentage": 41.67
}
```

Calculate values from the database. Use:

`occupancyPercentage = (occupiedSlots / totalSlots) * 100`

Reserved slots are **not** counted as occupied. Round the percentage to two decimal places. Handle a zero total safely, even though the initial system has 24 slots.

Zone-wise statistics should report the same counts for Zone A, Zone B, and Zone C.

### 6.5 Error Handling

Use a global exception handler (`@RestControllerAdvice`) and return understandable JSON errors. Handle at least:

- Slot not found.
- Sensor not found.
- Sensor mapped to a different slot.
- Invalid distance or request body.
- Invalid parking state transition.
- Invalid zone value.

Use appropriate HTTP status codes such as 400 for invalid requests and 404 for missing resources. Do not expose stack traces or internal database details in API responses.

### 6.6 CORS and Swagger

Allow the frontend origins `http://localhost:5173` and `http://localhost:3000` to call the backend.

Enable Swagger UI. Document the local Swagger URL in the final README instructions. The expected Springdoc URL is usually:

`http://localhost:8080/swagger-ui/index.html`

## 7. Frontend Requirements

Use React with Vite and Tailwind CSS. Keep components small and readable. Use a single API service/helper file for backend requests rather than scattering `fetch` calls throughout the UI.

The frontend should include:

- A main dashboard page.
- Summary cards for total, available, occupied, reserved, and occupancy percentage.
- Three zone sections with eight slot cards each.
- A slot details modal/panel.
- An IoT simulator panel with slot selection, sensor distance input/slider, and send-reading action.
- Vehicle entry and vehicle exit simulator shortcuts.
- Reserve, cancel reservation, and release actions.
- A recent events panel.
- Loading, empty, and error states.

Use an environment variable for the backend base URL, for example:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Provide a `.env.example` file. Do not hardcode the backend URL in multiple components.

The dashboard must obtain the slot list, statistics, zones, and events from the REST API. If the backend is unavailable, show a clear error and a retry option. Do not silently switch to fake data and present it as live backend data.

## 8. PostgreSQL Configuration and Initialization

Use a local PostgreSQL database named:

`smart_parking_db`

Default local connection details:

- Host: `localhost`
- Port: `5432`
- Database: `smart_parking_db`
- Username/password: read from environment variables or local configuration

Do not put a real password or secret in source control. Provide an `.env.example` or clearly documented environment variable names.

The backend must create the required tables through JPA configuration or a simple schema setup. Initialize the 24 parking slots only when they do not already exist. Initialization must be safe on every restart.

## 9. Running the Project Locally

Document exact commands for Windows PowerShell and/or IntelliJ IDEA. The expected run order is:

1. Install Java 17, Node.js, Maven (if not using the Maven wrapper), and PostgreSQL.
2. Create the database `smart_parking_db` in PostgreSQL.
3. Configure the backend database username/password using the documented environment variables or local properties.
4. Start the backend from the `backend` directory using the Maven wrapper if included, otherwise `mvn spring-boot:run`.
5. Start the frontend from the `frontend` directory using `npm install` followed by `npm run dev`.
6. Open the frontend URL printed by Vite (normally `http://localhost:5173`).
7. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html` to inspect and test APIs.

Include the exact database creation SQL and any required environment setup in the final project documentation. Do not assume the user already has the database configured.

## 10. Minimum Testing and Acceptance Criteria

The project is complete only when all of the following work:

1. The backend starts successfully and connects to PostgreSQL.
2. On first startup, exactly 24 slots are created: 8 in each zone.
3. Restarting the backend does not create duplicate slots or reset their statuses.
4. `GET /api/parking/slots` returns all 24 slots.
5. `GET /api/parking/stats` returns counts that match the database.
6. Sending an 8 cm reading for S05/P05 marks P05 as `OCCUPIED`.
7. Sending a 30 cm reading for S05/P05 marks P05 as `AVAILABLE`, provided it is not reserved.
8. Sending a reading with a mismatched sensor/slot pair is rejected.
9. A reserved slot remains `RESERVED` when the sensor reports no vehicle.
10. A vehicle detected in a reserved slot changes it to `OCCUPIED`.
11. Reservation and cancellation work and invalid state transitions are rejected.
12. Status changes create meaningful event history without duplicate events for repeated identical readings.
13. The frontend displays data received from the backend.
14. The frontend reflects a successful simulator action after the API call and refreshes the dashboard data.
15. The dashboard remains usable at normal laptop screen sizes and does not have broken layouts or non-functional controls.

Write a few focused backend tests for sensor threshold handling, state transitions, invalid sensor mapping, reservation behavior, and statistics. Run the tests and fix failures. Do not claim that an item has been tested unless it was actually run.

## 11. Simple Professor Demonstration Plan

The complete project should be demonstrable in approximately 2–3 minutes:

1. Open the dashboard and show 24 slots divided into three zones.
2. Point out the total, available, occupied, reserved, and occupancy statistics.
3. Open the IoT Simulator and select P05 (Sensor S05).
4. Send a distance reading of **8 cm**. Explain that the sensor detects a nearby vehicle. Show P05 changing to red/OCCUPIED and the statistics updating.
5. Send a distance reading of **30 cm**. Explain that the vehicle has left. Show P05 changing to green/AVAILABLE.
6. Reserve another available slot and then cancel its reservation.
7. Show the recent event history and explain that events are stored in PostgreSQL.
8. Optionally open Swagger UI to show the REST API endpoints.

### How to explain the IoT part

The current prototype uses a virtual ultrasonic sensor in the web-based simulator. The simulator sends the sensor ID, parking slot ID, and measured distance to the Spring Boot backend through a REST API. The backend applies the distance threshold, updates the corresponding parking slot in PostgreSQL, and returns the new status. The React dashboard retrieves the updated information and displays it.

Be clear that this version simulates the sensor in software; it does not use a physical ultrasonic sensor. The system is designed so that real sensor hardware could replace the simulator in a future extension.

## 12. Instructions for the AI Coding Agent (Antigravity)

Build the complete project from scratch using this README as the source of truth.

1. Read this entire README before creating files.
2. Create both the frontend and backend in the project structure described above.
3. Implement the required features end-to-end; do not stop after generating a design or sample code.
4. Keep the project simple and beginner-friendly. Do not add technologies or features outside the stated scope unless they are essential for the application to run.
5. Make the dashboard visually polished, but prioritize correct API integration and reliable behavior over decorative animations.
6. Do not use mock data as a substitute for backend functionality. Initial slot data must come from PostgreSQL through the backend.
7. Do not claim that the IoT simulator uses physical hardware. It is a software simulation.
8. Run backend compilation and tests, install frontend dependencies, and run a frontend build. Fix errors that can be fixed in the available environment.
9. If PostgreSQL is not available in the environment, still implement the complete configuration and explain exactly how to start PostgreSQL and verify the connection. Clearly report any verification that could not be performed.
10. Do not leave TODOs, placeholder buttons, non-functional controls, or incomplete API integrations in required features.
11. Avoid overwriting project files unnecessarily. Keep configuration and credentials out of version control.
12. At the end, provide the final folder structure, exact commands to run both applications, database setup steps, Swagger URL, API examples, test/build results, and any remaining limitations.

**Final priority:** A small project that works reliably in front of the professor is better than a complicated project with unfinished features. Implement only what is specified here, verify it, and keep the code easy to explain.
