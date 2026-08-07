# Microservices Architecture Study Guide — Spring Boot 4.1.0 & Spring Cloud 2025

This repository is designed as an educational masterclass project to study modern cloud-native microservices architecture using **Spring Boot 4.1.0** and **Spring Cloud 2025.1.2**. 

Rather than a simple monolith, this codebase breaks down business domains into single-responsibility services, demonstrating service discovery, gateway edge-routing, stateless token security, client-side load balancing, and clean external integrations.

---

## 1. System Architecture Map

```
                          [ Client Request ]
                                  |
                                  v (Host Port 8080)
                       +----------------------+
                       |     api-gateway      | <------- Registry Cache
                       +----------------------+               |
                         /        |         \                 |
                        /         |          \                |
                       v          v           v               v
                +-------------+ +-----------+ +------------+ +---------------+
                | auth-server | |ai-service | |task-service| | eureka-server |
                +-------------+ +-----------+ +------------+ +---------------+
                  (Port 9090)     (Port 9091)   (Port 9092)     (Port 8761)
```

---

## 2. Microservice Profiles

| Microservice | Subfolder / Source | Port | Architecture Pattern / Role | Key Technologies |
|--------------|-------------------|------|-----------------------------|------------------|
| [**`eureka-server`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/eureka-server) | `/eureka-server` | `8761` | **Service Registry & Directory** | Netflix Eureka Server |
| [**`api-gateway`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/api-gateway) | `/api-gateway` | `8080` | **Reverse Proxy & Routing Edge** | Webflux, Netty, Spring Cloud Gateway |
| [**`auth-server`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/auth-server) | `/auth-server` | `9090` | **Identity Provider & Token Issuer** | H2, BCrypt, HMAC-SHA256 JWT, Spring Security |
| [**`task-service`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/task-service) | `/task-service` | `9092` | **Resource Server (Task CRUD)** | JPA, Entity Managers, Global Advisers, Pageables |
| [**`ai-service`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/ai-service) | `/ai-service` | `9091` | **Resource Server (AI Integrations)** | Spring 6 RestClient, OpenRouter REST integration |

---

## 3. Core Study Modules

### Module A: Dynamic Service Registry & Heartbeats
*   **Study Target:** [**`eureka-server`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/eureka-server)
*   **The Problem it Solves:** Hardcoded IP address configuration is impossible in containerized environments due to dynamic IP allocations and scaling.
*   **How it Works:** Sibling services register their logical name (e.g. `AUTH-SERVER`) and container IP address on startup. Every 30s, they send heartbeats. If heartbeats cease, Eureka evicts the container from the directory map.
*   **Key Files to Read:** [`EurekaServerApplication.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/eureka-server/src/main/java/com/eureka/server/EurekaServerApplication.java) and [`application.yml`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/eureka-server/src/main/resources/application.yml).

### Module B: Reactive Routing & Client-side Load Balancing
*   **Study Target:** [**`api-gateway`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/api-gateway)
*   **The Problem it Solves:** Prevents clients from needing to coordinate requests to dozens of different ports and handles traffic redirection dynamically.
*   **How it Works:** Built on non-blocking Netty Webflux. Requests on port `8080` are analyzed against path predicates. The URI `lb://SERVICE-NAME` triggers Spring Cloud LoadBalancer to pick an instance from the Eureka cache and route the request.
*   **Key Files to Read:** [`api-gateway/pom.xml`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/api-gateway/pom.xml) and [`application.yml`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/api-gateway/src/main/resources/application.yml).

### Module C: Stateless Symmetric JWT Security
*   **Study Target:** [**`auth-server`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/auth-server) (Issuer) & [**`task-service`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/task-service) / [**`ai-service`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/ai-service) (Resource Servers)
*   **The Problem it Solves:** Monolithic user session storage does not scale horizontally across isolated backend nodes.
*   **How it Works:** 
    1.  `auth-server` validates credentials against an H2 database and returns a cryptographically signed JWT signed with a symmetric secret key (`JWT_SECRET`).
    2.  When calling `task-service` or `ai-service`, the client attaches `Authorization: Bearer <TOKEN>`.
    3.  The resource servers intercept the request, extract the token, and verify the cryptographic signature locally using the same `JWT_SECRET`. **No network call to auth-server or database lookup is needed**—the token payload (claims) is trusted implicitly.
*   **Key Files to Read:**
    - Issuer: [`SecurityConfig.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/auth-server/src/main/java/com/auth/server/config/SecurityConfig.java) and [`JwtUtil.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/auth-server/src/main/java/com/auth/server/security/JwtUtil.java).
    - Verifier Filter: [`JwtAuthenticationFilter.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/task-service/src/main/java/com/task/service/security/JwtAuthenticationFilter.java).

### Module D: Lightweight HTTP Integration (No Heavy SDKs)
*   **Study Target:** [**`ai-service`**](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/ai-service)
*   **The Problem it Solves:** Heavy vendor-specific SDK client libraries (like Spring AI BOMs) bring complex dependency trees that break compatibility during major framework upgrades (e.g. migrating to Spring Boot 4.1.0).
*   **How it Works:** Rather than importing OpenAI libraries, `ai-service` uses Spring 6's modern `RestClient` to dispatch raw POST requests directly to OpenRouter's `/v1/chat/completions` API endpoint, mapping JSON request/response payloads to standard Java `record` types.
*   **Key Files to Read:** [`AiProviderConfig.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/ai-service/src/main/java/com/ai/service/config/AiProviderConfig.java) and [`AiController.java`](file:///c:/Users/x_jack_ripper/Documents/SpringBoot/ai-service/src/main/java/com/ai/service/controller/AiController.java).

---

## 4. Suggested Code Reading Path for Students

To understand this system logically, read the code in this order:

1.  **`eureka-server`**: Understand how directories initialize (`@EnableEurekaServer`).
2.  **`auth-server`**: Understand user registration, H2 mapping, BCrypt password hashing, and JWT token signing.
3.  **`task-service`**: Inspect how symmetric JWT authentication filters intercept resources, and study standard CRUD structure mapping Entity to DTOs.
4.  **`ai-service`**: Study integration patterns using `RestClient` to connect to OpenRouter completions.
5.  **`api-gateway`**: Review routing predicate yaml mapping and understand how the edge routing proxy ties the entire system together.
6.  **`docker-compose.yml`**: Study network bridges and Docker actuator health checks.

---

## 5. Verification & Quick Testing Commands

### Launching Docker Environment
```bash
cp .env.example .env
docker compose up --build -d
```

### End-to-End API Flow Checks (Host Port 8080)
1.  **Check Service Registrations (Eureka):**
    `GET http://localhost:8761/eureka/apps`
2.  **Register a new user:**
    ```bash
    curl -X POST http://localhost:8080/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{"username":"devuser","password":"password123"}'
    ```
3.  **Login & Obtain JWT Token:**
    ```bash
    curl -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"devuser","password":"password123"}'
    ```
4.  **Create a Task (using Bearer Token):**
    ```bash
    curl -X POST http://localhost:8080/api/tasks \
      -H "Authorization: Bearer <TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"title":"Study Microservices","description":"Read the README files"}'
    ```
5.  **Test AI Chat completions (using Bearer Token):**
    ```bash
    curl -X POST http://localhost:8080/api/ai/chat \
      -H "Authorization: Bearer <TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"prompt":"Explain microservices in 5 words."}'
    ```