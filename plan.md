## Zorvyn Assignment Execution Plan

### 1) Understand Requirements
- Read the assignment PDF and convert each requirement into backend deliverables.
- Keep scope aligned to intern-level expectations: clear, complete, not over-engineered.

### 2) Architecture Decisions (Human-led)
- Use layered Spring Boot structure: controller -> service -> repository.
- Use JWT auth + role-based access (`ADMIN`, `ANALYST`, `VIEWER`).
- Use SQLite file persistence for zero-setup local execution.
- Include Swagger/OpenAPI for API visibility.

### 3) Implementation Approach
- Prioritize clear implementation of required features over extra complexity.
- Keep design ownership on role model, endpoint behavior, validation, and error handling.
- Review all generated and handwritten code against assignment requirements before submission.

### 4) Verification
- Use one real test script: `finance-api/test_api.sh`.
- Validate login, RBAC, records CRUD/filtering, dashboard visibility, and invalid-input handling.
- Confirm deployment endpoint behavior using the same test flow.

### 5) Documentation and Transparency
- Record assumptions and trade-offs in README.
- Take full responsibility for final review and submitted output.
