# CalcFlow Backend

## Overview
CalcFlow Backend is the Spring Boot API for the CalcFlow calculator application. It safely evaluates arithmetic and scientific expressions, stores successful results in MySQL, and exposes REST endpoints for calculation and history management.

## Features
- Safe expression evaluation without `eval`
- Support for arithmetic, parentheses, percentages, and square roots
- Calculation history persistence in MySQL
- Swagger/OpenAPI documentation
- Centralized validation and error handling
- JUnit 5 and Mockito test coverage

## Stack
- Java 21
- Spring Boot 3.3.x
- Maven
- MySQL 8
- Flyway
- Spring Data JPA
- Spring Validation
- Swagger UI / springdoc-openapi
- JUnit 5, Mockito, MockMvc, JaCoCo

## Architecture
The backend follows a layered design:
- `controller`: REST entry points
- `service`: business logic and validation
- `repository`: JPA persistence layer
- `entity`: database model
- `dto`: request/response contracts
- `exception`: centralized error handling
- `config`: future configuration classes

## Folder Structure
- `src/main/java/com/calcflow` — application code
- `src/main/resources` — configuration and migrations
- `src/test/java/com/calcflow` — backend tests
- `src/test/resources` — test configuration

## Prerequisites
- Java 21
- Maven
- MySQL 8
- Optional: Docker Compose

## Environment Setup
Create a `.env` file from `.env.example` and update the values:

```bash
cp .env.example .env
```

Then set:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`

## MySQL Setup
Create a database manually:

```sql
CREATE DATABASE calcflow;
CREATE USER 'calcflow_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON calcflow.* TO 'calcflow_user'@'localhost';
FLUSH PRIVILEGES;
```

Alternatively, with Docker Compose:

```bash
docker compose up -d mysql
```

## Run the app
```bash
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

If Maven is not available in PATH, use the local Maven wrapper path from your user profile or install Maven globally.

## Swagger UI
After starting the app, visit:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

## Testing
Run unit and integration tests:

```bash
mvn test
```

Generate coverage report:

```bash
mvn verify
```

## Build
```bash
mvn clean package
```

## Known limitations
- Scientific support is intentionally limited to square root, percentage, and exponentiation.
- History is shared globally by all users of the app.
- This project is focused on readability for learning.

## Future improvements
- Add advanced scientific functions
- Add pagination to history API
- Add user-level history entries
- Add CI/CD workflows
