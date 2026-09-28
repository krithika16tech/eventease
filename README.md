# EventEase — College Event Registration System

A Spring Boot RESTful backend application built for college event management, enabling organizers to create events with capacity limits and students to register for events with automatic capacity enforcement and cancellation rules.

---

## 1. Technology Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.3.4
* **Web**: Spring Web (Spring MVC, REST APIs)
* **Data Persistence**: Spring Data JPA, Hibernate ORM
* **Validation**: Spring Boot Starter Validation (Jakarta Validation)
* **Database**: MySQL 8.0
* **API Documentation**: Springdoc OpenAPI / Swagger UI (v2.5.0)
* **Build Tool**: Apache Maven

---

## 2. Project Architecture & Structure

```
c:\Eventease
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── eventease
│   │   │           ├── EventeaseApplication.java
│   │   │           ├── config
│   │   │           │   └── OpenApiConfig.java
│   │   │           ├── controller
│   │   │           │   ├── EventController.java
│   │   │           │   ├── RegistrationController.java
│   │   │           │   ├── StudentController.java
│   │   │           │   └── OrganizerController.java
│   │   │           ├── dto
│   │   │           │   ├── ErrorResponse.java
│   │   │           │   ├── EventCreateRequest.java
│   │   │           │   ├── EventResponse.java
│   │   │           │   ├── OrganizerCreateRequest.java
│   │   │           │   ├── OrganizerResponse.java
│   │   │           │   ├── RegistrationRequest.java
│   │   │           │   ├── RegistrationResponse.java
│   │   │           │   ├── StudentCreateRequest.java
│   │   │           │   └── StudentResponse.java
│   │   │           ├── entity
│   │   │           │   ├── Event.java
│   │   │           │   ├── Organizer.java
│   │   │           │   ├── Registration.java
│   │   │           │   ├── RegistrationStatus.java
│   │   │           │   └── Student.java
│   │   │           ├── exception
│   │   │           │   ├── CancellationNotAllowedException.java
│   │   │           │   ├── DuplicateRegistrationException.java
│   │   │           │   ├── EventFullException.java
│   │   │           │   ├── EventNotFoundException.java
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── InvalidOperationException.java
│   │   │           │   ├── OrganizerNotFoundException.java
│   │   │           │   ├── RegistrationNotFoundException.java
│   │   │           │   └── StudentNotFoundException.java
│   │   │           ├── repository
│   │   │           │   ├── EventRepository.java
│   │   │           │   ├── OrganizerRepository.java
│   │   │           │   ├── RegistrationRepository.java
│   │   │           │   └── StudentRepository.java
│   │   │           └── service
│   │   │               ├── EventService.java
│   │   │               ├── OrganizerService.java
│   │   │               ├── RegistrationService.java
│   │   │               └── StudentService.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java
│       │   └── com
│       │       └── eventease
│       │           ├── EventeaseApplicationTests.java
│       │           └── EventRegistrationIntegrationTests.java
│       └── resources
│           └── application-test.properties
└── test_live.ps1
```

---

## 3. Required Entities & Relationships

1. **`Organizer`**:
   - `id` (PK, Long, Auto-increment)
   - `name` (String, required)
   - `email` (String, unique, required)
   - `department` (String)
   - Relationship: `@OneToMany(mappedBy = "organizer")` -> `List<Event>`

2. **`Student`**:
   - `id` (PK, Long, Auto-increment)
   - `name` (String, required)
   - `email` (String, unique, required)
   - `rollNumber` (String, unique, required)
   - Relationship: `@OneToMany(mappedBy = "student")` -> `List<Registration>`

3. **`Event`**:
   - `id` (PK, Long, Auto-increment)
   - `title` (String, required)
   - `eventDate` (LocalDate, required)
   - `venue` (String, required)
   - `maxSeats` (Integer, positive, required)
   - Relationship: `@ManyToOne` -> `Organizer`
   - Relationship: `@OneToMany(mappedBy = "event")` -> `List<Registration>`

4. **`Registration`**:
   - `id` (PK, Long, Auto-increment)
   - Relationship: `@ManyToOne` -> `Student`
   - Relationship: `@ManyToOne` -> `Event`
   - `status` (Enum: `ACTIVE`, `CANCELLED`)
   - `registrationDate` (LocalDateTime)
   - `cancellationDate` (LocalDateTime)

---

## 4. Enforced Business Rules (Service Layer)

* **Rule 1 — Maximum Capacity**: When active registrations reach `maxSeats`, registration attempts are rejected with HTTP 400 and message `"Event '<title>' is full. Maximum capacity of <maxSeats> seats has been reached."`
* **Rule 2 — Cancellation Frees a Seat**: When a registration is cancelled, its status changes to `CANCELLED`. Only `ACTIVE` registrations count toward capacity, so a seat is immediately freed for another student.
* **Rule 3 — Duplicate Registration**: If a student already has an `ACTIVE` registration for an event, further registration attempts are rejected with HTTP 409 Conflict.
* **Rule 4 — Event Date Cancellation**: A student cannot cancel a registration on or after the event date (`LocalDate.now() >= event.getEventDate()`). Attempts are rejected with HTTP 400 Bad Request.
* **Student Ownership**: A student can only cancel their own registration.

---

## 5. REST API Endpoints

### Events
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/events` | Create a new event (Feature 1) | `201 CREATED` |
| `GET` | `/api/events` | List all events with available seats | `200 OK` |
| `GET` | `/api/events/{id}` | Get event details by ID | `200 OK` |
| `GET` | `/api/events/{id}/participants` | View registered participants (Feature 4) | `200 OK` |
| `GET` | `/api/events/{id}/registrations` | View registrations for an event | `200 OK` |

### Registrations
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/registrations` | Register a student for an event (Feature 2 & 3) | `201 CREATED` |
| `PUT` | `/api/registrations/{id}/cancel` | Cancel registration (Feature 5) | `200 OK` |
| `DELETE` | `/api/registrations/{id}` | Cancel registration (Feature 5) | `200 OK` |
| `GET` | `/api/registrations/{id}` | Get registration details | `200 OK` |
| `GET` | `/api/registrations/student/{studentId}` | Get student registrations | `200 OK` |

### Students & Organizers
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/students` | Create student | `201 CREATED` |
| `GET` | `/api/students` | List students | `200 OK` |
| `GET` | `/api/students/{id}` | Get student by ID | `200 OK` |
| `POST` | `/api/organizers` | Create organizer | `201 CREATED` |
| `GET` | `/api/organizers` | List organizers | `200 OK` |
| `GET` | `/api/organizers/{id}` | Get organizer by ID | `200 OK` |

---

## 6. Swagger UI & Interactive Documentation

Once the application is running, navigate to:
* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 7. How to Build and Run

### Build the project:
```bash
mvn clean package
```

### Run the application:
```bash
java -jar target/eventease-1.0.0.jar
```
Or with Maven:
```bash
mvn spring-boot:run
```

### Run tests:
```bash
mvn test
```
All 15 automated integration tests will execute, testing all normal and edge cases.
