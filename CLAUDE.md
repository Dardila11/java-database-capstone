# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

All Maven commands must be run from the `app/` directory.

```bash
cd app

# Run in development mode (Spring DevTools hot-reload)
./mvnw spring-boot:run

# Build JAR
./mvnw package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=PatientControllerTest

# Run a single test method
./mvnw test -Dtest=PatientControllerTest#loggedIn_returns200
```

### Docker (full stack)

```bash
cd app
docker compose up --build
```

This starts the Spring Boot app, MySQL (`cms` DB on port 3306), and MongoDB (port 27017). Requires a `.env` file with `JWT_SECRET` set.

## Environment Variables

All runtime config is injected via environment variables (see `app/src/main/resources/application.properties`):

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL (e.g. `jdbc:mysql://localhost:3306/cms?useSSL=false`) |
| `SPRING_DATASOURCE_USERNAME` | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password |
| `SPRING_DATA_MONGODB_URI` | MongoDB URI (e.g. `mongodb://localhost:27017/prescriptions`) |
| `JWT_SECRET` | Secret key for signing JWTs (must be set) |

## Architecture

### Dual-Database Design

- **MySQL** (Spring Data JPA / Hibernate): `Patient`, `Doctor`, `Appointment`, `Admin` — relational entities with `ddl-auto=update`.
- **MongoDB** (Spring Data MongoDB): `Prescription` documents in the `prescriptions` collection. `Prescription.java` uses `@Document`, not `@Entity`.

### Request Flow

```
HTTP → Controller → ValidationService (token) → Service / XxxService → Repository → DB
```

Controllers delegate auth to `ValidationService`, business logic to the appropriate domain service, and cross-cutting filtering/validation to the shared `Service` bean.

### Service Layer Structure

- **`ValidationService`** — all login validation and token assertion. Throws `InvalidCredentialsException` or `InvalidTokenException` (caught by `GlobalExceptionHandler`) rather than returning status codes.
- **`TokenService`** — JWT generation/validation using JJWT. Token subject is the user's email (or admin username). Role validation hits the DB on every check.
- **`Service`** (the generic bean) — cross-entity helpers: `extractToken`, `filterDoctor`, `validateAppointment` (returns int code), `filterPatient`.
- **`AppointmentService`**, **`DoctorService`**, **`PatientService`**, **`PrescriptionService`** — domain-specific CRUD and filtering.

### Authentication

- JWT is passed in the `Authorization: Bearer <token>` header for REST endpoints.
- Thymeleaf MVC dashboards receive the token as a `?token=<jwt>` query parameter.
- There is **no Spring Security filter chain** — token validation is manual, per-endpoint, via `ValidationService.validateToken(token, role)`.
- Passwords are BCrypt-hashed; `ValidationService` contains a plain-text migration path for legacy records (encode on first successful login).

### Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps all custom exceptions to JSON responses:
- `InvalidCredentialsException` → 401
- `InvalidTokenException` → 401
- `NotFoundException` → 404
- `MethodArgumentNotValidException` → 400 (field-level validation messages joined by `,`)
- `HttpMessageNotReadableException` → 400

### DTOs

- `AppointmentDTO` — flattened appointment view for API responses; hides entity graphs and sensitive fields. Has a static `from(Appointment)` factory.
- `AuthDTO` — contains nested records `LoginRequest`, `AdminLoginRequest`, `LoginResponse`.

### Frontend

Two frontend layers co-exist:

1. **Static HTML/JS SPA** (`app/src/main/resources/static/`) — vanilla HTML + CSS + JS. `index.html` is the entry point. JS is split into `js/config/` (API base URL), `js/services/` (API wrappers), and `js/components/` (DOM utilities).
2. **Thymeleaf dashboards** (`app/src/main/resources/templates/`) — server-rendered dashboards for `admin/`, `doctor/`, and `loggedPatient/` roles, served by `DashboardController`.

### Doctor Availability Logic

`DoctorService.getDoctorAvailability` computes free slots by comparing the doctor's `availableTimes` list (stored as `"HH:mm-HH:mm"` strings via `@ElementCollection`) against already-booked appointments on the requested date.

## Testing Patterns

- **Controller tests** use `@WebMvcTest` + `MockMvc` + `@MockitoBean` for service dependencies. See `PatientControllerTest` and `AdminControllerTest`.
- **Service tests** and **integration tests** exist alongside unit tests in `src/test/`.
- `@TestPropertySource(properties = "api.path=/")` is required on controller tests because `api.path` has no default.
