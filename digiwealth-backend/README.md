# DigiWealth AI — Backend

Spring Boot backend for the DigiWealth AI Avatar-Based Digital Wealth Management Platform.

## Tech Stack
Java 17 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · MySQL · Maven

## Modules Implemented
- **Auth**: register/login, JWT issuance, BCrypt password hashing, role-based authorization
- **Customer**: profile, dashboard (balance, income, expenses, savings, investments, active goals, health score)
- **Transactions**: add, list, filter by date/category, keyword search, monthly summary
- **Investments**: add holdings, portfolio view with profit/loss and asset allocation %
- **Goals**: create/update financial goals with progress tracking
- **Financial Health Score**: 0–100 score from savings ratio, expense ratio, investment ratio, debt ratio, emergency fund, and goal progress, plus actionable suggestions
- **AI Wealth Advisor**: builds a "Customer Context" prompt from real account data and calls OpenAI or Gemini; conversation is stored in `ai_chat`
- **Recommendations**: generated from financial-health suggestions and persisted

## Getting Started

### 1. Prerequisites
- JDK 17+
- Maven 3.9+
- MySQL 8+ running locally (or update `application.properties`)

### 2. Configure
Edit `src/main/resources/application.properties`:
- `spring.datasource.username` / `password` — your MySQL credentials
- `app.jwt.secret` — replace with a long random string (32+ chars) in production
- `app.ai.provider` — `openai` or `gemini`
- `app.ai.openai.api-key` / `app.ai.gemini.api-key` — set directly, or export as environment variables:
  ```bash
  export OPENAI_API_KEY=sk-...
  # or
  export GEMINI_API_KEY=...
  ```

### 3. Run
```bash
mvn spring-boot:run
```
The API starts on `http://localhost:8080`. Tables are auto-created via `spring.jpa.hibernate.ddl-auto=update`.

### 4. Try it (Postman / curl)
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","password":"password123"}'

# Login (returns JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123"}'

# Dashboard (use the token from login/register)
curl http://localhost:8080/api/customers/dashboard \
  -H "Authorization: Bearer <TOKEN>"

# Ask the AI Advisor
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"question":"How much should I save each month?"}'
```

## Notes on the AI integration
- `AiServiceImpl` builds a "Customer Context Builder" prompt (per the spec's AI Workflow) from live dashboard/investment/goal data, then calls whichever provider is configured.
- Swap providers anytime via `app.ai.provider` — no code changes needed.
- If the API key is left as the placeholder, calls will fail with a clear 503 error from `AiServiceException` rather than failing silently.

## Not yet implemented (flagged from the spec for a follow-up pass)
- Notifications module (SIP due, FD maturity, overspending alerts, etc.)
- Angular frontend (separate scaffold — ask if you'd like this built next)
- Scheduled jobs (e.g. recurring recommendation generation, spending-trend/anomaly detection)
- Audit logging, refresh tokens, rate limiting
