# Microservices System Architecture & Docker Orchestration

A multi-service architecture demonstrating authentication (JWT), Spring AI provider abstraction, task management (CRUD with JPA best practices), Eureka service discovery, and Spring Cloud Gateway routing containerized with Docker Compose.

---

## 1. Architecture Overview

This project consists of 5 standalone Spring Boot microservices, each fulfilling a single responsibility:

| Service Name | Container Port | Host Port | Single Responsibility |
|--------------|----------------|-----------|------------------------|
| **`eureka-server`** | 8761 | 8761 | **Service Registry**: Acts as a central directory where all running microservice instances register their container IP address and port. |
| **`api-gateway`** | 8080 | 8080 | **API Gateway**: Single entry point for external client traffic. Dynamically routes requests to backend services via Eureka service names (`lb://`). |
| **`auth-server`** | 9090 | 9090 | **Identity Provider**: Authenticates user credentials against an H2 database and issues signed HMAC-SHA256 JWT tokens. |
| **`ai-service`** | 9091 | 9091 | **AI Resource Server**: Provides Spring AI chat endpoints using OpenRouter. Validates JWT tokens from `auth-server`. |
| **`task-service`** | 9092 | 9092 | **CRUD Resource Server**: Manages Task entities with pagination and clean layered architecture. Validates JWT tokens from `auth-server`. |

---

## 2. Interaction Flows

### A. Authentication Flow (`/api/auth/*`)
1. **Client Request**: Client sends `POST http://localhost:8080/api/auth/login` with user credentials.
2. **Gateway Interception**: `api-gateway` receives the request on port 8080, matches route predicate `Path=/api/auth/**`.
3. **Eureka Name Resolution**: Gateway asks `eureka-server`: *"Where can I find an instance registered as `auth-server`?"* Eureka returns container network location `auth-server:9090`.
4. **Forwarding & Authentication**: Gateway forwards the payload to `auth-server:9090`. `auth-server` verifies the username and BCrypt password hash.
5. **Token Generation**: Upon successful verification, `auth-server` signs a JWT containing claims (subject=username, roles) using the shared `JWT_SECRET`.
6. **Response**: JWT token is returned back through `api-gateway` to the client.

### B. AI Service Flow (`/api/ai/*`)
1. **Client Request**: Client sends `POST http://localhost:8080/api/ai/chat` with `Authorization: Bearer <TOKEN>` and a prompt JSON.
2. **Gateway Interception & Resolution**: Gateway matches `Path=/api/ai/**`, queries Eureka for `ai-service`, and routes to container `ai-service:9091`.
3. **JWT Validation (Resource Server)**: `ai-service` intercepts the request in `JwtAuthenticationFilter`. It verifies the HMAC-SHA256 signature using the shared `JWT_SECRET` (matching `auth-server`'s key) and checks expiration. No database or `auth-server` network call is made—the JWT claims are trusted directly.
4. **Spring AI Provider Invocation**: The configured `ChatClient` constructs an OpenAI-compatible request payload and dispatches it to OpenRouter using `OPENROUTER_API_KEY`.
5. **Response**: AI-generated response is returned back through `api-gateway` to the client.

### C. Task CRUD Flow (`/api/tasks/*`)
1. **Client Request**: Client sends `GET http://localhost:8080/api/tasks?page=0&size=5` with `Authorization: Bearer <TOKEN>`.
2. **Gateway Interception & Resolution**: Gateway matches `Path=/api/tasks/**`, queries Eureka for `task-service`, and routes to container `task-service:9092`.
3. **JWT Validation (Resource Server)**: `task-service` verifies the JWT signature and expiration using `JWT_SECRET`.
4. **Business Logic & JPA**: `TaskService` processes the request, invoking `TaskRepository` to fetch a paginated list of `Task` entities, mapped into `TaskResponse` DTOs via `TaskMapper`.
5. **Response**: Paginated JSON payload is returned back through `api-gateway` to the client.

---

## 3. Service Registry Role

In traditional setups, microservice locations are hardcoded (e.g., `http://192.168.1.50:9090`). In containerized environments:
- Containers get dynamic IP addresses assigned by Docker DNS upon startup.
- Scale actions dynamically create and destroy instances.

**Eureka's Role**:
- **Registration**: When `auth-server` boots, its `spring-cloud-starter-netflix-eureka-client` sends a HTTP heartbeat to `http://eureka-server:8761/eureka/` declaring its application name (`auth-server`) and its container IP/port.
- **Heartbeat & Liveness**: Clients send periodic heartbeats (every 30s). If a container dies, Eureka evicts it from the registry map.
- **Lookup & Disambiguation**: `api-gateway` maintains a cached local copy of Eureka's registry. When routing `lb://auth-server`, it dynamically resolves the real target container address without needing static IP config.

---

## 4. How to Run Everything

### Step 1: Copy Environment Variables File
Create your `.env` file from the provided template:
```bash
cp .env.example .env
```
*(Optionally edit `.env` to insert your actual `OPENROUTER_API_KEY`)*

### Step 2: Build and Launch Containers
Run Docker Compose to build images and launch the containers:
```bash
docker compose up --build
```

### Expected Startup Order:
1. `eureka-server` container launches.
2. Docker Compose executes the Spring Boot Actuator `healthcheck` (`http://localhost:8761/actuator/health`).
3. Once `eureka-server` turns **healthy**, Docker Compose starts `auth-server`, `ai-service`, `task-service`, and `api-gateway`.
4. All services register themselves with Eureka within ~20–30 seconds.

---

## 5. Verification & Testing

### 1. Inspect Eureka Dashboard
Open your browser and navigate to:
```text
http://localhost:8761
```
Verify under **Instances currently registered with Eureka** that you see:
- `API-GATEWAY`
- `AUTH-SERVER`
- `AI-SERVICE`
- `TASK-SERVICE`

### 2. End-to-End API Calls via Gateway (Port 8080)

#### A. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"devuser","password":"password123"}'
```

#### B. Login & Obtain JWT
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"devuser","password":"password123"}' | jq -r '.token')

echo "JWT Token: $TOKEN"
```

#### C. Create & Fetch Tasks (Task CRUD Service)
```bash
# Create Task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Dockerize Microservices","description":"Complete container setup","status":"IN_PROGRESS"}'

# Get Paginated Tasks
curl -X GET "http://localhost:8080/api/tasks?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### D. Prompt AI Service
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Explain Docker container networking in 10 words."}'
```

---

## 6. Load Balancing Demonstration

To observe client-side Round-Robin load balancing across multiple service instances:

1. **Scale `task-service` to 2 instances**:
   ```bash
   docker compose up --scale task-service=2 -d
   ```
2. **Verify Registration**:
   Refresh `http://localhost:8761`. You will see `TASK-SERVICE` reporting 2 registered instances.
3. **Observe Round-Robin Routing**:
   Execute multiple requests to `http://localhost:8080/api/tasks`. Inspect the container logs:
   ```bash
   docker compose logs -f task-service
   ```
   You will observe `api-gateway` automatically alternating incoming requests evenly between container instance 1 and container instance 2 via Spring Cloud LoadBalancer.

---

## 7. Common Pitfalls & Solutions

1. **`localhost` vs Container Names**:
   - *Pitfall*: Configuring `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` inside a container.
   - *Reason*: Inside a container, `localhost` refers exclusively to that container's loopback interface (`127.0.0.1`), not the host computer or sibling containers.
   - *Solution*: Use Docker DNS container names: `http://eureka-server:8761/eureka/`.

2. **`depends_on` vs Actual Readiness**:
   - *Pitfall*: Relying on simple `depends_on: [eureka-server]` causes client services to crash because Docker starts the container process instantly, but Java takes ~10s to initialize the Spring context.
   - *Solution*: Combine `depends_on` with Actuator healthchecks: `condition: service_healthy`.

3. **Missing `.env` File**:
   - *Pitfall*: Container fails to start or JWT validation fails due to missing environment variables.
   - *Solution*: Ensure `.env` exists in the root directory before running `docker compose up`.
#   s p r i n b o o t - c o m p l e t e  
 #   s p r i n b o o t - c o m p l e t e  
 #   s p r i n b o o t - c o m p l e t e  
 #   s p r i n b o o t - c o m p l e t e  
 