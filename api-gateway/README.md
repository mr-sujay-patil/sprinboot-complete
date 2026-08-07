# API Gateway Microservice — Edge Routing Masterclass

`api-gateway` serves as the entry point (front door) for all client traffic. It is built on Spring Cloud Gateway (Webflux-based reactive stack) rather than classic MVC.

---

## 1. Core Concepts Demonstrated

### A. Edge Routing (Reverse Proxy)
Instead of forcing clients to connect to individual microservices (e.g. `9090` for auth, `9092` for tasks), clients make all calls to port `8080`. The Gateway analyzes request properties and proxies them downstream:
- `http://localhost:8080/api/auth/**` -> forwarded to `auth-server`
- `http://localhost:8080/api/tasks/**` -> forwarded to `task-service`
- `http://localhost:8080/api/ai/**` -> forwarded to `ai-service`

### B. Client-side Load Balancing (`lb://`)
The routing configuration specifies targets using `lb://SERVICE-NAME`. 
*   **Mechanism:** Rather than routing to a static IP, the Gateway intercepts the service name, queries its local Eureka registry cache, and selects an active container instance.
*   **Algorithms:** Out of the box, Spring Cloud LoadBalancer uses a Round-Robin algorithm to alternate requests evenly across scaled instances.

---

## 2. Technical Framework Breakdown

### Webflux Reactive Model
Unlike `auth-server` or `task-service` which use Tomcat and thread-per-request blocking architectures, `api-gateway` runs on Netty. 
*   **Why:** A reactive gateway uses a small number of event loop threads to handle thousands of concurrent requests asynchronously without blocking.
*   **Precaution:** You cannot mix standard Spring Web MVC with Spring Cloud Gateway in the same POM—it requires the Webflux reactive starter.

### Configuration Properties (`application.yml`)
Under Spring Cloud Gateway 2025.x, Webflux routing properties have a distinct prefix:
```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: task-route
              uri: lb://task-service
              predicates:
                - Path=/api/tasks/**
```

---

## 3. Study Guide: Dynamic Route Inspection
You can inspect active routing paths using the Actuator endpoint exposed on the Gateway:

*   **Endpoint:** `GET http://localhost:8080/actuator/gateway/routes`
*   **Result:** A JSON list containing predicates, filters, and resolved metadata for all active route patterns.
