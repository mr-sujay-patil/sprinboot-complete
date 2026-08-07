# AI Service Microservice

A Spring Boot + Spring AI microservice acting as a JWT-secured resource server for AI chat functionalities.

## Architecture Overview

This project is part of a two-service architecture designed for learning modern Spring security and AI integration:
- **auth-server (port 9090)**: Responsible for authenticating users and issuing signed JWTs on login/register.
- **ai-service (port 9091)**: Acts as a Resource Server. Validates JWTs natively and provides AI chat endpoints.

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- A valid OpenRouter API key (`OPENROUTER_API_KEY`).

## Quick Start

1. **Set your API key**:
   ```bash
   export OPENROUTER_API_KEY=your-actual-key
   ```
2. **Run the service**:
   ```bash
   cd ai-service
   mvn spring-boot:run
   ```

## End-to-End Flow

The sequence of a complete request looks like this:

```text
Client                     Auth Server (9090)              AI Service (9091)
  |                              |                              |
  |--- POST /api/auth/login ---->|                              |
  |<--- JWT token ---------------|                              |
  |                              |                              |
  |--- POST /api/ai/chat --------|----------------------------->|
  |    Authorization: Bearer <token>                            |
  |                              |    1. Extract Bearer token   |
  |                              |    2. Validate signature     |
  |                              |       (same shared secret)   |
  |                              |    3. Check expiration       |
  |                              |    4. Set SecurityContext    |
  |                              |    5. Forward to controller  |
  |                              |    6. Call OpenRouter        |
  |<--- AI response -------------|------------------------------|
```

### Steps to test:

**a. Register on auth-server**
```bash
curl -X POST http://localhost:9090/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser", "password":"password123"}'
```

**b. Login to get token**
```bash
# Capture the returned JWT string
curl -X POST http://localhost:9090/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser", "password":"password123"}'
```

**c. Use token to call ai-service's /api/ai/chat**
```bash
curl -X POST http://localhost:9091/api/ai/chat \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{"prompt":"Explain dependency injection in 5 words."}'
```

## Provider Configuration

The service uses OpenRouter as its exclusive AI provider via Spring AI's OpenAI-compatible HTTP client.

| Provider   | active-provider value | Env Var for API Key | Model Example                    |
|------------|-----------------------|---------------------|----------------------------------|
| OpenRouter | `openrouter`          | `OPENROUTER_API_KEY`| meta-llama/llama-3.1-8b-instruct |

## How JWT Validation Works

This service acts as a stateless resource server. It uses the **SAME** `jwt.secret` as the auth server. 
The custom `JwtAuthenticationFilter` intercepts the request:
1. Extracts the Bearer token from the `Authorization` header.
2. Validates the HMAC-SHA256 signature and token expiration using the `jjwt` library.
3. Sets the Spring `SecurityContext`.

> **Note**: No `UserDetailsService` or database calls are needed in this microservice — it intrinsically trusts the validated token claims.

## Limitations

As a learning project, there are a few intentional limitations:
- Single shared secret (symmetric key) instead of asymmetric RS256 keypairs.
- No refresh tokens implemented.
- No rate limiting or quotas on the AI endpoints.
