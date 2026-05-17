# Test Setup Explained

## Two Distinct Test Patterns

The codebase uses **two different setups** depending on what is being tested.

---

## Pattern 1 — Controller Tests (`@WebMvcTest`)

Used in: `AdminControllerTest`, `PatientControllerTest`, `DoctorControllerTest`, `AppointmentControllerTest`

```java
@WebMvcTest(AdminController.class)
@TestPropertySource(properties = "api.path=/")
@DisplayName("AdminController — POST /admin/login")
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ValidationService validationService;
```

| Annotation / Field | What it does |
|---|---|
| `@WebMvcTest(X.class)` | Starts a **partial** Spring context — only the web layer (controllers, filters, `GlobalExceptionHandler`). Does NOT load `@Service`, `@Repository`, or the full app. Much faster than `@SpringBootTest`. |
| `@TestPropertySource(...)` | Injects a test-only property. Required here because `api.path` has no default value in `application.properties` — the context would fail to start without it. |
| `@DisplayName(...)` | Human-readable label shown in test reports. No effect on execution. |
| `@Autowired MockMvc` | Spring auto-configures a `MockMvc` instance that simulates HTTP requests without starting a real server. |
| `@Autowired ObjectMapper` | Jackson's serializer/deserializer — used to convert model objects to JSON strings for request bodies. |
| `@MockitoBean` | Creates a Mockito mock **and registers it as a Spring bean**, replacing any real bean of that type in the context. This is how you prevent the controller from actually calling the database. |

---

## Pattern 2 — Service Tests (`@ExtendWith(MockitoExtension.class)`)

Used in: `TokenServiceTest`, `PatientServiceTest`, `AppointmentServiceTest`

```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private TokenService tokenService;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void stubDoctorLookup() { ... }
```

| Annotation / Field | What it does |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Tells JUnit 5 to activate Mockito's lifecycle hooks. This is what makes `@Mock` and `@InjectMocks` work. **No Spring context is loaded at all** — pure unit test. |
| `@Mock` | Creates a Mockito mock of the type. All methods return `null`/`0`/`false` by default. You control behavior with `when(...).thenReturn(...)`. |
| `@InjectMocks` | Instantiates the class under test and **automatically injects the `@Mock` fields** into its constructor or fields. No Spring DI involved. |
| `@BeforeEach` | Runs before every `@Test` in the same class or `@Nested` block. Used to set up shared state or stubs that every test needs. |

---

## Shared Structural Annotations

Both patterns use these:

| Annotation | What it does |
|---|---|
| `@Test` | Marks a method as a test case to be run by JUnit 5. |
| `@Nested` | Groups related tests into a named inner class. Helps organize by endpoint or method. `@Nested` classes share the outer class's fields (`MockMvc`, mocks, etc). |
| `@DisplayName(...)` | Labels the test in reports. Convention here is to describe the scenario (e.g. `"returns 200 with token in body"`). |

---

## Special Case: `ReflectionTestUtils` in `TokenServiceTest`

```java
@BeforeEach
void injectSecret() {
    ReflectionTestUtils.setField(tokenService, "jwtSecret", TEST_SECRET);
}
```

`TokenService` has a `@Value("${JWT_SECRET}") String jwtSecret` field — a Spring-injected value. Since there is no Spring context in this test, `@InjectMocks` cannot inject it. `ReflectionTestUtils.setField` bypasses Java's access control to set the private field directly.

---

## How a Controller Test Flows End-to-End

```
mockMvc.perform(post("/admin/login").content(...))
    │
    ├─ Spring MVC routes to AdminController.login()
    │
    ├─ Controller calls validationService.validateAdminLogin(...)
    │     └─ This is a @MockitoBean — returns "mocked.jwt.token" (as configured by when())
    │
    ├─ Controller builds ResponseEntity and returns it
    │
    └─ .andExpect(status().isOk()) verifies the HTTP response
```

No real database, no real JWT validation — everything outside the controller is stubbed.
