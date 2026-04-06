# Zorvyn Assignment

This repository contains my submission for the Zorvyn screening assignment.

## Contents
- `finance-api/` - Spring Boot backend project for the finance dashboard APIs
- `Zorvyn Screening Portal.pdf` - assignment/problem statement document

## Project Overview
The backend implements:
- JWT-based authentication
- Role-based access control (`ADMIN`, `ANALYST`, `VIEWER`)
- Financial records APIs (create, update, delete, list, filter)
- Dashboard summary API
- Validation and exception handling
- SQLite persistence

## Tech Stack
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- SQLite
- Maven

## How to Run
From the `finance-api` folder:

```bash
mvn spring-boot:run
```

Swagger/OpenAPI:
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

## Default Login
- Email: `admin@zorvyn.com`
- Password: `admin123`

## Notes
- Detailed API usage and test script instructions are available in `finance-api/README.md`.

## Deploy On Render
This repo now includes Maven Wrapper, so Render can build without a preinstalled local Maven setup.

- `finance-api/mvnw`
- `finance-api/mvnw.cmd`
- `finance-api/.mvn/wrapper/*`

You can deploy either with `render.yaml` (recommended) or manually in Render dashboard:

- Root Directory: `finance-api`
- Build Command: `./mvnw clean package -DskipTests`
- Start Command: `java -jar target/finance-api-0.0.1-SNAPSHOT.jar --server.port=$PORT`
