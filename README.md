# Smart Clinic Management System

A full-stack web application for managing clinic operations, exposing a Spring Boot REST API backed by MySQL and MongoDB, with a static HTML/JS frontend and Thymeleaf-rendered dashboards for admins, doctors, and logged-in patients.

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
| Frontend | Vanilla HTML + CSS + JavaScript |

---

## Prerequisites

- Java 17+
- MySQL with a database named `cms`
- MongoDB (default port 27017)

---



---

## Running the Application

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

Authentication uses JWT tokens passed as path variables. Tokens encode the user's email and are valid for 7 days. Roles: `admin`, `doctor`, `patient`.

---

## API Endpoints

### Admin
| Method | Path | Description |
|---|---|---|
| POST | `/admin/login` | Login, returns JWT |

### Doctor
| Method | Path | Description |
|---|---|---|
| GET | `/doctor` | List all doctors |
| GET | `/doctor/filter/{name}/{time}/{speciality}` | Filter doctors |
| GET | `/doctor/{userId}/{doctorId}/{date}/{token}` | Get doctor with availability |
| POST | `/doctor/{token}` | Create doctor (admin only) |
| PUT | `/doctor/{token}` | Update doctor (admin only) |
| DELETE | `/doctor/{doctorId}/{token}` | Delete doctor (admin only) |

### Patient
| Method | Path | Description |
|---|---|---|
| POST | `/patient` | Register patient |
| POST | `/patient/login` | Login, returns JWT |
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

### MVC Dashboards
| Method | Path | Description |
|---|---|---|
| GET | `/adminDashboard/{token}` | Admin dashboard (Thymeleaf) |
| GET | `/doctorDashboard/{token}` | Doctor dashboard (Thymeleaf) |
| GET | `/loggedPatientDashboard/{token}` | Patient dashboard (Thymeleaf) |

---

## Project Structure

```
java-database-capstone/
├── CLAUDE.md
├── README.md
├── schema-architecture.md
└── app/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/project/back_end/
        │   │   ├── BackEndApplication.java
        │   │   ├── config/
        │   │   ├── controllers/
        │   │   ├── mvc/
        │   │   ├── models/
        │   │   ├── repo/
        │   │   ├── services/
        │   │   └── DTO/
        │   └── resources/
        │       ├── application.properties
        │       ├── static/          # Patient-facing HTML/CSS/JS
        │       └── templates/
        │           ├── admin/       # Admin dashboard
        │           ├── doctor/      # Doctor dashboard
        │           └── loggedPatient/  # Patient dashboard
        └── test/
```