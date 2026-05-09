# Task Manager (Spring Boot)

## Requirements
- Java 17
- Maven 3.9+

## What this project shows (resume-friendly)
- JWT login/register (Spring Security)
- MongoDB (local or Atlas)
- Task CRUD (secured endpoints)
- OpenAI REST call on task creation (priority + summary + first-step suggestion)

## Run
```bash
mvn spring-boot:run
```

## Environment variables (recommended)
- `MONGODB_URI` (optional): your MongoDB Atlas URI
- `JWT_SECRET` (optional): a long random secret
- `OPENAI_API_KEY` (optional): your OpenAI key (if not set, AI falls back to defaults)

## API endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/tasks` (requires JWT)
- `GET /api/tasks` (requires JWT)
- `PUT /api/tasks/{taskId}` (requires JWT)
- `DELETE /api/tasks/{taskId}` (requires JWT)

## Test
```bash
mvn test
```

