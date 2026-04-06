# Zorvyn Finance API (Intern-Level Assignment)

This project is a Spring Boot backend for a finance dashboard assignment.  
It covers user roles, financial records CRUD/filtering, dashboard summary APIs, access control, validation, and persistence.

## Project Highlights
- Layered Spring Boot backend (`controller -> service -> repository`) with clean separation of concerns
- JWT auth + role-based access control (`ADMIN`, `ANALYST`, `VIEWER`)
- File-based SQLite persistence for zero-setup local run
- Swagger/OpenAPI for API visibility and quick manual verification
- Deployed on Render for live endpoint access
- End-to-end verification using `test_api.sh` against local or deployed API (`BASE_URL`)

## Tech Stack
- Java 17
- Spring Boot 3.2 (Web, Security, Validation, JPA)
- SQLite (file-based: `finance.db`)
- JWT auth
- Swagger/OpenAPI (`springdoc`)
- Maven
- Render (deployment)
- Bash + curl + python (`test_api.sh` execution)

## Roles
- `ADMIN`: full access (users + records + dashboard)
- `ANALYST`: read records + dashboard
- `VIEWER`: dashboard only

## Quick Start
1. Run the app:
   ```bash
   mvn spring-boot:run
   ```
2. Open Swagger:
   - `http://localhost:8080/swagger-ui/index.html`
   - `http://localhost:8080/v3/api-docs`
3. Login via `POST /api/auth/login`:
   ```json
   {
     "email": "admin@zorvyn.com",
     "password": "admin123"
   }
   ```
4. Copy `token` and use Swagger **Authorize** with:
   ```
   Bearer <your_token>
   ```

## Seeded User
- Email: `admin@zorvyn.com`
- Password: `admin123`

## Run API Integration Script
The script validates core behavior end-to-end with `curl`:

```bash
bash test_api.sh
```

Optional environment overrides:
- `BASE_URL` (default `http://localhost:8080`)
- `ADMIN_EMAIL` (default `admin@zorvyn.com`)
- `ADMIN_PASSWORD` (default `admin123`)

## Why This Design
- Uses straightforward Spring layering (`controller -> service -> repository`) for readability.
- Uses JWT + role checks to demonstrate backend access control clearly.
- Uses SQLite file DB to show persistence without setup overhead.
- Keeps scope aligned with assignment requirements, avoiding over-engineering.

## Known Limitations
- No refresh token flow
- No pagination/search extras
- Basic test script assumes `python` is installed

## Deploy on Render
Use Maven Wrapper for consistent build tooling.

- Build command:
  - `./mvnw clean package -DskipTests`
- Start command:
  - `java -jar target/finance-api-0.0.1-SNAPSHOT.jar --server.port=$PORT`

If deploying from the repository root, set Render Root Directory to `finance-api`.
