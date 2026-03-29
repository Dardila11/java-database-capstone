# CLAUDE.md — Smart Clinic Management System

This file provides guidance for AI assistants (and developers) working on this codebase.

---

## Project Overview

**Smart Clinic Management System** is a full-stack web application for managing clinic operations. It exposes a Spring Boot REST API backed by a dual-database architecture (MySQL + MongoDB) and serves a static HTML/JS frontend with some Thymeleaf-rendered dashboard views.

**Role model:** Admin, Doctor, Patient — all authenticated via JWT tokens.

---

## Repository Layout

```
java-database-capstone/
├── CLAUDE.md                        # This file
├── README.md                        # Minimal project description
├── schema-architecture.md           # Architecture narrative
└── app/                             # Maven project root
    ├── pom.xml
    ├── mvnw / mvnw.cmd              # Maven wrapper
    └── src/
        ├── main/
        │   ├── java/com/project/back_end/
        │   │   ├── BackEndApplication.java       # Entry point
        │   │   ├── config/WebConfig.java         # CORS config
        │   │   ├── controllers/                  # REST controllers
        │   │   ├── mvc/DashboardController.java  # Thymeleaf MVC
        │   │   ├── models/                       # JPA entities + Mongo document
        │   │   ├── repo/                         # Spring Data repositories
        │   │   ├── services/                     # Business logic
        │   │   └── DTO/                          # Data transfer objects
        │   └── resources/
        │       ├── application.properties
        │       ├── static/                       # HTML/CSS/JS frontend
        │       └── templates/                    # Thymeleaf templates
        └── test/
            └── java/com/project/back_end/
                └── BackEndApplicationTests.java
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 |
| Build | Maven (Maven Wrapper included) |
| Relational DB | MySQL (Spring Data JPA / Hibernate) |
| Document DB | MongoDB (Spring Data MongoDB) |
| Auth | JWT (JJWT 0.12.6) |
| Templating | Thymeleaf (admin & doctor dashboards) |
| Frontend | Vanilla HTML + CSS + JavaScript |

---

## Running the Application

### Prerequisites

- Java 17+
- A running MySQL instance with a database named `cms`
- A running MongoDB instance (default port 27017)

### Configuration

All runtime configuration lives in `app/src/main/resources/application.properties`. Before running, set these values (or override via environment variables):

```properties
spring.datasource.url=jdbc:mysql://<mysql_host>/cms?usessl=false
spring.datasource.username=root
spring.datasource.password=<mysql_password>

spring.data.mongodb.uri=mongodb://root:<mongodb_password>@<mongodb_host>:27017/prescriptions?authSource=admin

jwt.secret=$!@#$^%$$$%####$DDCPN0234FCFDPD8670M    # change in production
```

### Build and Run

```bash
cd app
./mvnw spring-boot:run          # development (hot-reload via DevTools)
./mvnw package                  # produces app/target/back-end-0.0.1-SNAPSHOT.jar
java -jar target/back-end-0.0.1-SNAPSHOT.jar
```

### Run Tests

```bash
cd app
./mvnw test
```

> **Note:** The only test currently present is a Spring context load test (`BackEndApplicationTests`). Both databases must be reachable (or mocked) for it to pass.

---

## Architecture

### Data Flow

```
HTTP Request
  → Controller   (input validation, role check via Service.validate*)
  → Service      (business logic, delegates to repositories)
  → Repository   (Spring Data JPA or MongoDB)
  → Database     (MySQL or MongoDB)
```

### Dual-Database Strategy

- **MySQL** stores all structured relational data: `Patient`, `Doctor`, `Appointment`, `Admin`.
- **MongoDB** stores flexible prescription documents (`Prescription` collection `prescriptions`).

Hibernate DDL is set to `update` — schema changes are applied automatically on startup. There are no migration scripts.

### Authentication

All protected endpoints receive a JWT token (typically as a path variable `{token}`). The `Service` class (shared service) validates the token and resolves the caller's role before delegating to role-specific services. Token generation/validation is in `TokenService`.

- Tokens are valid for **7 days**.
- The JWT encodes the user's email, which is used to look up the user record.
- Role authorization: `admin`, `doctor`, `patient` checked per-endpoint.

### CORS

`WebConfig` permits all origins (`*`), all standard HTTP methods, and all headers. This is intentionally permissive for development; tighten for production.

---

## Package Conventions

### `models/`

JPA entities use `@Entity` + `@Table`. MongoDB document uses `@Document(collection = "prescriptions")`.

- Validation annotations (`@NotBlank`, `@Email`, `@Size`, `@Pattern`) are on model fields.
- Password fields: `@JsonProperty(access = WRITE_ONLY)` — never serialized in responses.
- `Appointment` has `@Transient` derived fields (`endTime`, `appointmentDate`, `appointmentTimeOnly`) computed from `appointmentTime`.
- `Doctor.availableTimes` is an `@ElementCollection` stored as a separate table.

### `repo/`

All repositories extend either `JpaRepository` (MySQL) or `MongoRepository` (MongoDB). Custom query methods use Spring Data naming conventions or `@Query` annotations.

Notable custom methods:
- `DoctorRepository.findByNameContainingIgnoreCase`
- `AppointmentRepository` — filtering queries combining doctor, date, patient name, status
- `PrescriptionRepository.findByAppointmentId`

### `services/`

- `Service.java` — shared service injected across controllers; handles token validation and appointment filtering logic.
- `TokenService` — `@Component` for JWT creation and parsing.
- Other services (`PatientService`, `DoctorService`, `AppointmentService`, `PrescriptionService`) contain business logic and call repositories directly.

### `controllers/`

- REST controllers use `@RestController`.
- `DashboardController` (in `mvc/`) uses `@Controller` and returns Thymeleaf view names.
- `ValidationFailed` is a `@RestControllerAdvice` that intercepts `MethodArgumentNotValidException` and returns a `400` map of field errors.
- Token is passed as a `@PathVariable` on most endpoints.

### `DTO/`

- `Login` — holds `email` and `password` for login requests.
- `AppointmentDTO` — flattened view of an appointment (avoids circular serialization, hides sensitive fields).

---

## API Endpoints Reference

### Admin

| Method | Path | Description |
|---|---|---|
| POST | `/admin/login` | Admin login, returns JWT |

### Doctor

| Method | Path | Description |
|---|---|---|
| GET | `/doctor` | List all doctors |
| GET | `/doctor/filter/{name}/{time}/{speciality}` | Filter doctors |
| GET | `/doctor/{userId}/{doctorId}/{date}/{token}` | Get doctor with availability for date |
| POST | `/doctor/{token}` | Create doctor (admin only) |
| PUT | `/doctor/{token}` | Update doctor (admin only) |
| DELETE | `/doctor/{doctorId}/{token}` | Delete doctor (admin only) |

### Patient

| Method | Path | Description |
|---|---|---|
| POST | `/patient` | Register patient |
| POST | `/patient/login` | Patient login, returns JWT |
| GET | `/patient/{token}` | Get patient by token |
| GET | `/patient/{patientId}/{token}/appointments` | Get patient's appointments |
| GET | `/patient/{patientId}/{token}/filter` | Filter patient's appointments |

### Appointment

| Method | Path | Description |
|---|---|---|
| GET | `/appointments/{date}/{patientName}/{token}` | Doctor's appointments (filtered) |
| POST | `/appointments/{token}` | Book appointment |
| PUT | `/appointments/{token}` | Update appointment |
| DELETE | `/appointments/{appointmentId}/{token}` | Cancel appointment |

### Prescription

| Method | Path | Description |
|---|---|---|
| POST | `/prescription/{token}` | Save prescription (doctor only) |
| GET | `/prescription/{appointmentId}/{token}` | Get prescription by appointment |

### MVC Dashboard Views

| Method | Path | Template |
|---|---|---|
| GET | `/adminDashboard/{token}` | `templates/admin/adminDashboard.html` |
| GET | `/doctorDashboard/{token}` | `templates/doctor/doctorDashboard.html` |

---

## Frontend Structure

Static assets are served from `src/main/resources/static/`.

```
static/
├── index.html                       # Landing / role selection
├── pages/                           # Patient-facing HTML pages
├── js/
│   ├── config/config.js             # API base URL config
│   ├── services/                    # API call wrappers (fetch)
│   ├── components/                  # Reusable DOM-building functions
│   └── *.js                         # Page-level scripts
└── css/                             # Per-page stylesheets
```

Admin and Doctor dashboards are served as Thymeleaf views (under `templates/`); all patient-facing pages are static HTML with JavaScript fetching the REST API.

---

## Key Conventions

1. **Token as path variable** — Most protected endpoints take `{token}` as the last path segment. Always validate role before performing the operation.
2. **Appointment status** — `0` = scheduled, `1` = completed. No enum — use integer constants.
3. **No migration scripts** — Schema is managed by Hibernate `ddl-auto=update`. Do not drop/recreate tables manually unless resetting the environment.
4. **MongoDB IDs are Strings** — `Prescription.id` is a `String` mapped to MongoDB's `ObjectId`.
5. **DTO for responses** — Use `AppointmentDTO` when returning appointment data to avoid circular references and to hide patient/doctor passwords.
6. **Validation errors** — `ValidationFailed` advice returns `Map<String, String>` with field names as keys. Do not add additional exception handlers that conflict with it.
7. **Password storage** — Passwords are currently stored in plain text. Do not add hashing without also updating all login comparison logic.
8. **CORS** — Currently open for all origins. Do not narrow without testing the frontend.

---

## Known Limitations / Tech Debt

- Passwords stored in plain text (no BCrypt or equivalent).
- JWT secret is hardcoded in `application.properties` — must be externalized for production.
- No Docker or container configuration exists; databases must be provisioned manually.
- Test coverage is minimal — only a context load test exists.
- Hibernate `ddl-auto=update` is not suitable for production; consider Flyway or Liquibase.
- CORS is fully open (`*`) — restrict for production deployments.
- No API versioning strategy in place.
