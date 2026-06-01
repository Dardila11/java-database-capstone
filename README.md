# Smart Clinic Management System

A full-stack web application for managing clinic operations, exposing a Spring Boot REST API backed by MySQL and MongoDB, with a static HTML/JS frontend and Thymeleaf-rendered dashboards for admins, doctors, and patients.
Use spring modulith

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 |
| Build | Maven (wrapper included) |
| Relational DB | MySQL (Spring Data JPA / Hibernate) |
| Document DB | MongoDB (Spring Data MongoDB) |
| Auth | JWT (JJWT 0.12.6) |
| Templating | Thymeleaf |
| Frontend | Vanilla HTML + CSS + JavaScript, React |

---

## Prerequisites

- Docker and Docker Compose (Recommended)
- OR Java 17+, MySQL (database `cms`), and MongoDB (port 27017)

---

## Configuration

All runtime configuration lives in `app/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://<mysql_host>/cms?usessl=false
spring.datasource.username=root
spring.datasource.password=<mysql_password>

spring.data.mongodb.uri=mongodb://root:<mongodb_password>@<mongodb_host>:27017/prescriptions?authSource=admin

jwt.secret=${JWT_SECRET}
```

Environment variables for Docker can be configured in the `.env` file at the root of the project.

---

## Running the Application

### Using Docker (Recommended)

You can run the entire application stack (Frontend, Backend, MySQL, MongoDB) using Docker Compose:

```bash
# Start all services in the background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```
Once started, the frontend will be available at `http://localhost` and the backend API at `http://localhost:8080`.

### Manual Setup (Without Docker)

```bash
cd app
./mvnw spring-boot:run          # development (hot-reload via DevTools)
./mvnw package                  # build JAR
java -jar target/back-end-0.0.1-SNAPSHOT.jar
```

### Run Tests

```bash
cd app
./mvnw test
```

---

## Architecture

### Data Flow

```
HTTP Request
  → Controller   (input validation, role check)
  → Service      (business logic)
  → Repository   (Spring Data JPA or MongoDB)
  → Database     (MySQL or MongoDB)
```

**MySQL** stores structured relational data: `Patient`, `Doctor`, `Appointment`, `Admin`.  
**MongoDB** stores flexible prescription documents (`prescriptions` collection).

### Authentication

Login endpoints return a JWT token valid for 7 days. All protected endpoints require the token in the `Authorization` request header. Roles: `admin`, `doctor`, `patient`.

---

## API Endpoints

> Protected endpoints require an `Authorization` header with a valid JWT token.

### Admin

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/admin/login` | No | Login, returns JWT |

### Doctor

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/doctor/login` | No | Doctor login, returns JWT |
| GET | `/doctor` | No | List all doctors |
| GET | `/doctor/filter/{name}/{time}/{speciality}` | No | Filter doctors by name, time, and specialty |
| GET | `/doctor/availability/{user}/{doctorId}/{date}` | Yes | Get doctor availability for a date |
| POST | `/doctor/` | Yes (admin) | Create doctor |
| PUT | `/doctor/` | Yes (admin) | Update doctor |
| DELETE | `/doctor/{id}` | Yes (admin) | Delete doctor |

### Patient

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/patient/create` | No | Register new patient |
| POST | `/patient/login` | No | Patient login, returns JWT |
| GET | `/patient/` | Yes (patient) | Get current patient details |
| GET | `/patient/{id}` | Yes (patient) | Get patient appointments by ID |
| GET | `/patient/filter/{condition}/{name}` | Yes (patient) | Filter patient appointments |

### Appointment

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/appointments/{date}/{patientName}/` | Yes (doctor) | Get appointments by date and patient name |
| POST | `/appointments/` | Yes (patient) | Book appointment |
| PUT | `/appointments/` | Yes (patient) | Update appointment |
| DELETE | `/appointments/{id}` | Yes (patient) | Cancel appointment |

### Prescription

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/prescription/` | Yes (doctor) | Save prescription |
| GET | `/prescription/{appointmentId}` | Yes | Get prescription by appointment |

### MVC Dashboards

> Token is passed as a query parameter: `?token=<jwt>`

| Method | Path | Description |
|---|---|---|
| GET | `/adminDashboard?token={token}` | Admin dashboard (Thymeleaf) |
| GET | `/doctorDashboard?token={token}` | Doctor dashboard (Thymeleaf) |
| GET | `/loggedPatientDashboard?token={token}` | Patient dashboard (Thymeleaf) |

---

## Project Structure

```
java-database-capstone/
├── CLAUDE.md
├── README.md
├── docker-compose.yml
├── .env
├── schema-architecture.md
├── react-frontend/       # React frontend application
└── app/
    ├── Dockerfile        # Backend Docker configuration
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/project/back_end/
        │   │   ├── BackEndApplication.java
        │   │   ├── config/
        │   │   ├── controllers/          # REST controllers
        │   │   ├── mvc/                  # Thymeleaf MVC controller
        │   │   ├── models/               # JPA entities + MongoDB document
        │   │   ├── repo/                 # Spring Data repositories
        │   │   ├── services/             # Business logic
        │   │   └── DTO/                  # Data transfer objects
        │   └── resources/
        │       ├── application.properties
        │       ├── static/
        │       │   ├── index.html        # Landing / role selection
        │       │   ├── pages/            # Patient-facing HTML pages
        │       │   │   ├── addPrescription.html
        │       │   │   ├── patientAppointments.html
        │       │   │   ├── patientDashboard.html
        │       │   │   ├── patientRecord.html
        │       │   │   └── updateAppointment.html
        │       │   ├── react/            # React frontend build
        │       │   ├── assets/           # CSS and images
        │       │   └── js/               # JavaScript modules
        │       │       ├── config/       # API base URL config
        │       │       ├── services/     # API call wrappers
        │       │       └── components/   # Reusable DOM components
        │       └── templates/
        │           ├── admin/            # Admin dashboard template
        │           ├── doctor/           # Doctor dashboard template
        │           └── loggedPatient/    # Patient dashboard template
        └── test/
```
