# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**Smart Clinic Management System** is a full-stack web application for managing clinic operations. It exposes a Spring Boot REST API backed by a dual-database architecture (MySQL + MongoDB) and serves a static HTML/JS frontend with some Thymeleaf-rendered dashboard views.

**Role model:** Admin, Doctor, Patient — all authenticated via JWT tokens.

---

## Repository Layout

```
java-database-capstone/
├── CLAUDE.md
├── README.md
├── schema-architecture.md
└── app/                             # Maven project root
    ├── pom.xml
    ├── docker-compose.yml
    ├── mvnw / mvnw.cmd
    └── src/
        ├── main/
        │   ├── java/com/project/back_end/
        │   │   ├── BackEndApplication.java
        │   │   ├── config/           # WebConfig (CORS), SecurityConfig (BCrypt bean)
        │   │   ├── controllers/      # REST controllers
        │   │   ├── mvc/              # DashboardController (Thymeleaf)
        │   │   ├── models/           # JPA entities + Mongo document
        │   │   ├── repo/             # Spring Data repositories
        │   │   ├── services/         # Business logic
        │   │   ├── exceptions/       # Custom exceptions + GlobalExceptionHandler
        │   │   └── DTO/              # AuthDTO, AppointmentDTO
        │   └── resources/
        │       ├── application.properties
        │       ├── static/           # HTML/CSS/JS frontend
        │       └── templates/        # Thymeleaf templates
        └── test/
            └── java/com/project/back_end/
                ├── AdminControllerTest.java       # @WebMvcTest slice test
                ├── AppointmentServiceTest.java    # @ExtendWith(MockitoExtension)
                ├── PatientServiceTest.java        # @ExtendWith(MockitoExtension)
                ├── TokenServiceTest.java
                ├── BackEndApplicationTests.java   # Context load test (needs DBs)
                └── controllers/
                    └── PatientControllerTest.java
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
| Passwords | BCrypt (`spring-security-crypto`) |
| Templating | Thymeleaf (admin & doctor dashboards) |
| Frontend | Vanilla HTML + CSS + JavaScript |

---

## Running the Application

### Prerequisites

- Java 17+
- MySQL and MongoDB (run via Docker Compose or provision manually)

### Docker Compose (recommended)

```bash
cd app
# Requires a .env file with JWT_SECRET set
docker compose up --build
```

The compose file starts `mysql:8.0`, `mongo:6.0`, and the Spring Boot app on port 8080. It wires the datasource URLs automatically via environment variables.

### Manual / Local Dev

All runtime configuration lives in `app/src/main/resources/application.properties` and is driven entirely by environment variables:

```
SPRING_DATASOURCE_URL=jdbc:mysql://<host>/cms?createDatabaseIfNotExist=true&useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=<password>
SPRING_DATA_MONGODB_URI=mongodb://<host>:27017/prescriptions
JWT_SECRET=<secret>
```

```bash
cd app
./mvnw spring-boot:run    # hot-reload via DevTools
```

### Build Commands

```bash
cd app
./mvnw package                                          # build JAR
./mvnw test                                             # run all tests
./mvnw test -Dtest=AppointmentServiceTest               # run one test class
./mvnw test -Dtest=AppointmentServiceTest#returns1*     # run one test method
```

> **Note:** `BackEndApplicationTests` (context load test) requires live MySQL and MongoDB connections. All other tests (`@WebMvcTest`, `@ExtendWith(MockitoExtension.class)`) run without databases.

---

## Architecture

### Data Flow

```
HTTP Request
  → Controller         (delegates auth to ValidationService)
  → ValidationService  (throws typed exceptions; never returns null on failure)
  → Service            (business logic, delegates to repositories)
  → Repository         (Spring Data JPA or MongoDB)
  → Database           (MySQL or MongoDB)
```

### Dual-Database Strategy

- **MySQL** stores all structured relational data: `Patient`, `Doctor`, `Appointment`, `Admin`.
- **MongoDB** stores flexible prescription documents (`Prescription` collection `prescriptions`).

Hibernate DDL is set to `update` — schema changes are applied automatically on startup.

### Authentication & Authorization

All protected endpoints receive a JWT token (typically as a path variable `{token}`).

- **`ValidationService`** — central auth service injected into controllers. Validates credentials and tokens; throws `InvalidCredentialsException` or `InvalidTokenException` on failure (never returns null).
- **`TokenService`** — `@Component` for JWT creation (`generateToken`) and parsing (`extractEmail`, `validateToken`).
- Tokens are valid for **7 days** and encode the user's email.
- Role authorization: `admin`, `doctor`, `patient` checked per-endpoint via `ValidationService.validateToken(token, role)`.

### Password Handling

Passwords use BCrypt (`SecurityConfig` exposes a `PasswordEncoder` bean). `ValidationService` applies a **lazy migration** on login: if the stored password is plain-text, it BCrypt-hashes it and re-saves before issuing the token.

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) centralizes all error responses:

| Exception | HTTP Status | Body key |
|---|---|---|
| `InvalidCredentialsException` | 401 | `error` |
| `InvalidTokenException` | 401 | `error` |
| `NotFoundException` | 404 | `error` |
| `MethodArgumentNotValidException` | 400 | `error` (field:message CSV) |
| `HttpMessageNotReadableException` | 400 | `error` |
| `Exception` (catch-all) | 500 | `error` |

Do not add additional `@ExceptionHandler` methods in controllers — route all exceptions through `GlobalExceptionHandler`.

### CORS

`WebConfig` permits all origins (`*`), all standard HTTP methods, and all headers. Intentionally permissive for development.

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

- `ValidationService` — handles all login and token validation; throws typed exceptions; inject this in controllers for auth checks.
- `Service.java` — older shared service; contains appointment filtering logic.
- `TokenService` — JWT generation/parsing only.
- `PatientService`, `DoctorService`, `AppointmentService`, `PrescriptionService` — business logic per domain, call repositories directly.

### `DTO/`

- `AuthDTO` — nested records: `AdminLoginRequest` (username + password), `LoginRequest` (email + password), `LoginResponse` (token). All fields carry `@NotBlank`/`@Email` validation.
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

Admin and Doctor dashboards are Thymeleaf views (under `templates/`); all patient-facing pages are static HTML with JavaScript fetching the REST API.

---

## Key Conventions

1. **Token as path variable** — Most protected endpoints take `{token}` as the last path segment. Call `ValidationService.validateToken(token, role)` before performing the operation.
2. **Appointment status** — `0` = scheduled, `1` = completed. No enum — integer constants only.
3. **No migration scripts** — Schema is managed by Hibernate `ddl-auto=update`.
4. **MongoDB IDs are Strings** — `Prescription.id` is a `String` mapped to MongoDB's `ObjectId`.
5. **DTO for responses** — Use `AppointmentDTO` when returning appointment data to avoid circular references and to hide patient/doctor passwords.
6. **Validation errors** — `GlobalExceptionHandler` returns `Map<String, String>` with a single `"error"` key. All `@Valid`-annotated controller parameters produce a 400 with field errors joined as `field:message` CSV in that key.

---

## Test Patterns

Two test styles are used; choose based on what you're testing:

**Service unit tests** (`@ExtendWith(MockitoExtension.class)`): use `@Mock` + `@InjectMocks`. No Spring context. Fast, no database needed. See `AppointmentServiceTest`, `PatientServiceTest`.

**Controller slice tests** (`@WebMvcTest(XController.class)`): use `@MockitoBean` to stub service dependencies. MockMvc drives HTTP. Requires `@TestPropertySource(properties = "api.path=/")`. See `AdminControllerTest`, `PatientControllerTest`.

---

## Known Limitations / Tech Debt

- Hibernate `ddl-auto=update` is not suitable for production; consider Flyway or Liquibase.
- CORS is fully open (`*`) — restrict for production deployments.
- No API versioning strategy in place.
- Some older passwords may still be stored as plain text until the user logs in (lazy BCrypt migration).
