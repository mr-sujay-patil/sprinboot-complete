# Eureka Service Registry — Service Discovery Masterclass

`eureka-server` acts as the central directory/phonebook of the entire microservices system. It enables dynamic service-to-service communication without hardcoded hostnames or IP addresses.

---

## 1. Core Concepts Demonstrated

### A. Dynamic Service Discovery
In a containerized or cloud environment, container IP addresses are dynamic. When the system scales or containers restart:
- Static IPs are impossible to maintain.
- Eureka resolves this by mapping logical **Application Names** (like `AUTH-SERVER` or `TASK-SERVICE`) to their dynamic IP addresses and ports.

### B. Heartbeats and Liveness Check
*   **Registration:** Sibling microservices send a `POST` request to Eureka on startup to register their network location.
*   **Renewals (Heartbeats):** Every 30 seconds, registered clients send a `PUT` request to renew their lease.
*   **Eviction:** If Eureka does not receive a heartbeat for 90 seconds (default), it assumes the instance is dead and removes it from the registry.

### C. Self-Preservation Mode
*   **Mechanism:** If Eureka detects a sudden drop in heartbeats (more than 15% drop within a renewal period, often due to a network partition), it enters **Self-Preservation Mode**.
*   **Behavior:** During self-preservation, Eureka stops evicting expired instances. This prevents it from dropping healthy instances due to temporary network issues.

---

## 2. Technical Framework Breakdown

### Key Annotations
*   **`@EnableEurekaServer`**: Placed on the main Spring Application class to activate the Eureka Server registry configuration.

### Critical Configurations (`application.yml`)
*   `eureka.client.register-with-eureka: false`: Prevents the registry server from attempting to register with itself.
*   `eureka.client.fetch-registry: false`: Prevents the registry server from pulling other registries (not needed for a standalone directory node).

---

## 3. Study Guide: Inter-service Communication

```
  +--------------+                    +--------------+
  |  auth-server | --- Registers ---->| eureka-server|
  +--------------+                    +--------------+
                                             ^
                                         Resolves
                                         "lb://"
                                             |
                                      +-------------+
                                      | api-gateway |
                                      +-------------+
```

1. **Registration Phase:** `auth-server`, `ai-service`, and `task-service` fetch Eureka's location and register their internal container IPs.
2. **Query Phase:** `api-gateway` pulls the list of active services and caches it locally.
3. **Routing Phase:** When a request arrives, `api-gateway` intercepts `lb://task-service` and dynamically routes the traffic using the cached IP address.

---

## 4. How to Study and Inspect
1. Start the service.
2. Open the **Eureka Dashboard** in your browser at `http://localhost:8761`.
3. Inspect:
   - **DS Replicas**: Standalone configuration details.
   - **Instances currently registered**: Lists the logical Application IDs, dynamic IPs, and status state (`UP`).
   - **General Info**: Displays memory utilization, CPU count, and uptime stats.
