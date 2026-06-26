#  Student Management System (SMS)

A **REST API** built with **Spring Boot** for managing student admissions, courses, and enrollments — developed as part of the Platform Commons Backend Software Developer assignment.

---

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Swagger UI](#swagger-ui)
- [Postman Collection](#postman-collection)
- [ER Diagram](#er-diagram)
- [API Contract](#api-contract)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5 |
| Language | Java 25 |
| Database | MySQL 8 |
| ORM | JPA with Hibernate 6 |
| Security | Spring Security + JWT (Auth0 java-jwt) |
| API Docs | Swagger / SpringDoc OpenAPI 2.8 |
| Build Tool | Maven |
| Mapping | ModelMapper |
| Validation | Jakarta Bean Validation |
| Utilities | Lombok |

---

## 📁 Project Structure

```
student-management-system/
├── src/main/java/com/platformcommons/sms/
│   ├── advice/                  # Global response wrapper (ApiResponse, ApiError)
│   │   ├── ApiError.java
│   │   ├── ApiResponse.java
│   │   └── GlobalResponseHandler.java
│   ├── config/                  # App configuration beans
│   │   ├── ModelMapperConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/              # REST controllers
│   │   ├── AuthController.java
│   │   └── StudentController.java
│   ├── dto/                     # Request / Response DTOs
│   │   ├── AuthResponse.java
│   │   ├── CourseRequest.java
│   │   ├── CourseResponse.java
│   │   ├── EnrollmentRequest.java
│   │   ├── LoginRequest.java
│   │   ├── StudentRequest.java
│   │   ├── StudentResponse.java
│   │   └── StudentUpdateRequest.java
│   ├── entity/                  # JPA entities
│   │   ├── enums/
│   │   │   ├── AddressType.java
│   │   │   ├── CourseType.java
│   │   │   └── Gender.java
│   │   ├── Address.java
│   │   ├── Course.java
│   │   ├── CourseTopic.java
│   │   ├── Student.java
│   │   ├── StudentCourse.java
│   │   └── UserCredential.java
│   ├── exception/               # Custom exceptions & global handler
│   │   ├── DuplicateResourceException.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── repository/              # Spring Data JPA repositories
│   │   ├── CourseRepository.java
│   │   ├── StudentCourseRepository.java
│   │   ├── StudentRepository.java
│   │   └── UserCredentialRepository.java
│   ├── security/                # JWT filter
│   │   └── JwtAuthFilter.java
│   ├── service/                 # Service interfaces & implementations
│   │   ├── impl/
│   │   │   └── StudentServiceImpl.java
│   │   ├── JWTService.java
│   │   ├── LoginService.java
│   │   └── StudentService.java
│   └── SmsApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── data.sql                 # Admin user seed
└── pom.xml
```

---

## ✨ Features

### Admin Operations
- ✅ Register a new student (auto-provisions login credentials)
- ✅ Create courses with topics
- ✅ Enroll a student into a course (duplicate check → 409 CONFLICT)
- ✅ Search students by name (paginated, case-insensitive)
- ✅ Get all students enrolled in a specific course (paginated)

### Student Operations
- ✅ View own profile (with enrolled courses)
- ✅ Update profile — email, mobile, parent names, addresses
- ✅ Search courses by name (paginated)
- ✅ Search courses by topic (paginated)
- ✅ Unenroll from a course

### Security
- ✅ JWT-based stateless authentication
- ✅ Role-based access control (`ROLE_ADMIN` / `ROLE_STUDENT`)
- ✅ Admin credentials stored in DB (BCrypt hashed)
- ✅ Student login: `username = studentCode`, `password = dateOfBirth (YYYY-MM-DD)`

### Good-to-Have (Implemented)
- ✅ Swagger / OpenAPI UI
- ✅ DTO layer with ModelMapper
- ✅ Global response wrapper (`ApiResponse<T>`)
- ✅ Global exception handler with structured error responses
- ✅ Bean validation on all request DTOs
- ✅ Pagination & sorting on all list endpoints

---

## ✅ Prerequisites

- Java 17+ (tested on Java 25)
- Maven 3.8+
- MySQL 8.0+
- (Optional) Postman for API testing

---

## ⚙️ Setup & Installation

### 1. Clone the repository

```bash
git clone https://github.com/buildwithtushar/student-management-system.git
cd student-management-system
```

### 2. Create the MySQL database

```sql
CREATE DATABASE sms_db;
```

### 3. Configure `application.properties`

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sms_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.algorithm.key=your_secret_key_here
jwt.issuer=your_issuer_name
jwt.expiry.duration=3600000

# SQL seed file
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql
```

### 4. Admin seed (`src/main/resources/data.sql`)

The file seeds the default admin user on startup:

```sql
INSERT IGNORE INTO user_credentials (username, password, role)
VALUES ('admin', '<bcrypt_hash_of_your_password>', 'ROLE_ADMIN');
```

> 💡 To generate a BCrypt hash for your chosen password, start the app once and hit `GET /api/v1/auth/hash?plain=yourpassword` (temporary utility endpoint), or use any online BCrypt generator.

---

## ▶️ Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

The server starts at: **`http://localhost:8080`**

---

## 🔐 Authentication

All protected endpoints require a **Bearer token** in the `Authorization` header:

```
Authorization: Bearer <token>
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "timeStamp": "2026-06-27T01:00:00",
  "data": {
    "token": "eyJhbGci...",
    "role": "ADMIN"
  },
  "error": null
}
```

### Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` (or as configured in `data.sql`) |
| Student | `<studentCode>` e.g. `STU001` | `<dateOfBirth>` e.g. `2000-05-15` |

> Student credentials are **auto-provisioned** when the admin registers a student.

---

## 📡 API Endpoints

### Auth

| Method | URL | Role | Description |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Login and receive JWT token |

### Student – Admin Endpoints

| Method | URL | Role | Description |
|---|---|---|---|
| POST | `/api/v1/students/register` | ADMIN | Register a new student |
| POST | `/api/v1/students/courses/create` | ADMIN | Create a new course |
| POST | `/api/v1/students/enroll-student-in-course` | ADMIN | Enroll student in course |
| GET | `/api/v1/students/search-by-name?name=John` | ADMIN | Search students by name |
| GET | `/api/v1/students/enrolled-in-course/{courseId}` | ADMIN | Get students in a course |

### Student – Student Endpoints

| Method | URL | Role | Description |
|---|---|---|---|
| GET | `/api/v1/students/{studentId}/profile` | ADMIN / STUDENT | Get student profile |
| PUT | `/api/v1/students/{studentId}/update-profile` | STUDENT | Update own profile |
| GET | `/api/v1/students/courses/search-by-name?courseName=Java` | STUDENT | Search courses by name |
| GET | `/api/v1/students/courses/search-by-topic?topicName=OOP` | STUDENT | Search courses by topic |
| DELETE | `/api/v1/students/{studentId}/unenroll-from-course/{courseId}` | STUDENT | Leave a course |

### Pagination Query Parameters (all list endpoints)

| Param | Default | Description |
|---|---|---|
| `pageNo` | `1` | Page number (1-based) |
| `pageSize` | `10` | Items per page |
| `sortBy` | `id` | Field to sort by |
| `sortDir` | `asc` | `asc` or `desc` |

---

## 📖 Swagger UI

Once the app is running, access the interactive API documentation at:

**`http://localhost:8080/swagger-ui.html`**

or

**`http://localhost:8080/swagger-ui/index.html`**

The Swagger UI lets you:
- Browse all endpoints grouped by tag
- See request/response schemas
- Execute API calls directly from the browser (use the **Authorize** button to pass your JWT token)

---

## 📬 Postman Collection

A ready-to-use Postman collection is included in the repository:

📁 `postman/SMS_Collection.json`

[Admin Operations.postman_collection.json](../Admin%20Operations.postman_collection.json)

[Login Operation.postman_collection.json](../Login%20Operation.postman_collection.json)

[Student Operations.postman_collection.json](../Student%20Operations.postman_collection.json)

### Import steps:
1. Open Postman
2. Click **Import** → **Upload Files**
3. Select `SMS_Collection.json`
4. Set the `base_url` collection variable to `http://localhost:8080`
5. Run **Login** first to get a token, then test other endpoints

---

## 🗄️ ER Diagram

The Entity-Relationship diagram is available at:

📁 `docs/ER_Diagram.png`

![Screenshot 2026-06-27 011644.png](../../OneDrive/Pictures/Screenshots/Screenshot%202026-06-27%20011644.png)

**Entity Relationships:**
- `Student` ↔ `Address` — **One-to-Many** (a student can have multiple addresses: PERMANENT, CORRESPONDENCE, CURRENT)
- `Student` ↔ `Course` — **Many-to-Many** via `StudentCourse` join table
- `Course` ↔ `CourseTopic` — **One-to-Many** (a course has multiple topics)
- `Student` ↔ `UserCredential` — **One-to-One** (auto-created on registration)

---

## 📄 API Contract

A detailed API contract (request/response schemas for every endpoint) is available at:

📁 `docs/API_Contract.xlsx`

[SMS_Entities.xlsx](../API%20Contract/SMS_Entities.xlsx)

[SMS_APIs.xlsx](../API%20Contract/SMS_APIs.xlsx)

It covers:
- Endpoint name, HTTP method, URL
- Role required
- Full request body with field-level details
- Sample response body

---

## 🏗️ Design Decisions

- **Global Response Wrapper** — All controller responses are wrapped in `ApiResponse<T>` via `GlobalResponseHandler` (a `ResponseBodyAdvice`) so the client always gets a consistent `{ timeStamp, data, error }` envelope.
- **Auto-provisioning credentials** — When admin registers a student, a `UserCredential` row is automatically created with `username = studentCode` and `password = BCrypt(dateOfBirth)`.
- **Stateless JWT** — No sessions. Every request is authenticated via the `JwtAuthFilter` which validates the Bearer token and sets the `SecurityContext`.
- **Soft duplicate prevention** — Enrollment duplicates are caught at the service layer before hitting the DB unique constraint, returning a clean `409 CONFLICT` with a descriptive message.
- **EntityGraph on profile fetch** — `@EntityGraph` is used on `findById` to eagerly load `studentCourses` and their `course` in a single query, avoiding N+1.

---

## 📞 Contact

For any queries regarding this assignment:
- **Tushar Sinha** — tushar.sinha.build@gmail.com
