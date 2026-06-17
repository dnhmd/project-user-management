# User Management API

A production-grade User Management REST API engineered with Java 21, Spring Boot 3.5, and PostgreSQL to demonstrate
enterprise-grade architecture, scalable security, and modern backend patterns.

### Why I Built This

Having recently built a similar system in Python based on the OneUpTime architecture, I wanted to challenge myself to
bring those same production standards over to Java.

It had been a while since I built a comprehensive project in Spring Boot, and I missed working within its powerful
ecosystem. I chose to build this to reconnect with Java 21, push my Spring Boot skills to the next level, and create a
rock-solid starter template for any future Java microservices I want to spin up.

## Features

- JWT authentication with access and refresh tokens
- Role-based access control (ADMIN / USER)
- User CRUD with ownership enforcement
- Password reset flow
- Request validation
- Rate limiting (in-memory, Bucket4j)
- Global exception handling
- Flyway database migrations
- Unit tests (JUnit 5 + Mockito)

## Tech Stack

- **Java 21** (Eclipse Temurin)
- **Spring Boot 3.5**
- **Spring Security 6**
- **Spring Data JPA**
- **PostgreSQL 16**
- **Flyway**
- **Lombok**
- **JJWT 0.12.6**
- **Bucket4j**
- **JUnit 5 + Mockito**

## Prerequisites

- Java 21 (via sdkman: `sdk install java 21-tem`)
- Maven 3.9+ (via sdkman: `sdk install maven`)
- Docker

## Getting Started

### 1. Start PostgreSQL

```bash
docker run --name usermanagement-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres_password \
  -e POSTGRES_DB=user-management \
  -p 5433:5432 \
  -d postgres:16
```

### 2. Configure Environment Variables (Optional)

The app uses sensible defaults for local development. Override in production:

| Variable      | Description                    | Default                                   |
|---------------|--------------------------------|-------------------------------------------|
| `DB_USERNAME` | Database username              | `postgres`                                |
| `DB_PASSWORD` | Database password              | `postgres_password`                       |
| `SECRET_KEY`  | JWT signing key (min 32 chars) | `change-me-in-production-must-be-32chars` |

### 3. Run the Application

```bash
mvn spring-boot:run
```

Flyway will automatically run all migrations on startup. A default admin user is seeded on first run.

**Default Admin Credentials:**

- Email: `admin@app.com`
- Password: `Admin@Password123`

## API Reference

Base URL: `http://localhost:8080/api/v1`

### Auth Endpoints (Public)

| Method | Endpoint                | Description               |
|--------|-------------------------|---------------------------|
| POST   | `/auth/register`        | Register a new user       |
| POST   | `/auth/login`           | Login and receive tokens  |
| POST   | `/auth/refresh`         | Refresh access token      |
| POST   | `/auth/forgot-password` | Request password reset    |
| POST   | `/auth/reset-password`  | Reset password with token |
| POST   | `/auth/logout`          | Revoke refresh token      |

### User Endpoints (Authenticated)

| Method | Endpoint               | Role           | Description               |
|--------|------------------------|----------------|---------------------------|
| GET    | `/users`               | ADMIN          | Get all users (paginated) |
| GET    | `/users/{id}`          | OWNER or ADMIN | Get user by ID            |
| POST   | `/users`               | ADMIN          | Create a user             |
| PATCH  | `/users/{id}`          | OWNER          | Update user name/email    |
| PATCH  | `/users/{id}/password` | OWNER          | Change password           |
| PATCH  | `/users/{id}/role`     | ADMIN          | Change user role          |
| DELETE | `/users/{id}`          | OWNER          | Soft delete user          |

### Authentication

All protected endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer <access_token>
```

### Example Requests

**Register**

```json
POST /api/v1/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password@123"
}
```

**Login**

```json
POST /api/v1/auth/login
{
  "email": "john@example.com",
  "password": "Password@123"
}
```

**Refresh Token**

```json
POST /api/v1/auth/refresh
{
  "refreshToken": "<refresh_token>"
}
```

**Get Users (Admin)**

```
GET /api/v1/users?page=0&limit=10&name=john&isActive=true
Authorization: Bearer <access_token>
```

### Password Requirements

Passwords must be 8-20 characters and contain:

- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`@$!%*?&`)

## Rate Limiting

Requests are rate limited to **10 requests per minute** per IP address. Exceeding this returns `429 Too Many Requests`.

## Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2026-06-14T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found: 99"
}
```

| Status | Meaning                              |
|--------|--------------------------------------|
| 400    | Bad request / validation error       |
| 401    | Unauthorized / invalid credentials   |
| 403    | Forbidden / insufficient permissions |
| 404    | Resource not found                   |
| 409    | Conflict (e.g. email already in use) |
| 429    | Too many requests                    |
| 500    | Internal server error                |

## Running Tests

```bash
mvn test
```

17 unit tests across service layer (UserService + AuthService).

## Project Structure

```
src/main/java/com/dnhmd/user_management/
├── config/          # App config, security config, data initializer
├── controller/      # REST controllers
├── dto/             # Request/response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions, global handler
├── mapper/          # Entity to DTO mappers
├── repository/      # Spring Data JPA repositories
├── security/        # JWT service, filters, entry points
└── service/         # Business logic interfaces and implementations
 
src/main/resources/
├── db/migration/    # Flyway SQL migrations
└── application.yml  # Application configuration
```

## Database Migrations

| Version | Description                                |
|---------|--------------------------------------------|
| V1      | Create roles, users, refresh_tokens tables |
| V2      | Fix FK column types to bigint              |
| V3      | Insert default roles (ADMIN, USER)         |
 
