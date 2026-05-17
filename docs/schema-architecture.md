# Section 1: Architecture summary

This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.

# Section 2: Numbered flow of data and control

1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropiate Thymeleaf or REST controller.
3. When a user interact with the application the request is routed to a backend controller based on the URL path and the HTTP method.
4. The controllers delegate logic to the Service layer which acts as the heat of the backend system.
5. The service layer communicate with the repository layer to perform data access operations.
6. Each repository interfaces directly with the underlying database engine.
7. Once data is retrieve from database, it is mapped into Java model classes that the application can work with.
