# AI Service — REST API Integration Masterclass

`ai-service` provides natural language chat completions. It acts as a stateless JWT-secured Resource Server, and communicates with the external OpenRouter API using Spring 6's modern `RestClient`.

---

## 1. Core Concepts & Architectural Decisions

### A. Custom HTTP Client vs. Heavy SDK Dependency
Historically, microservices integrated with AI vendors via proprietary client libraries or heavy multi-framework abstractions (like Spring AI).
*   **The Problem:** Heavy SDKs introduce deep class hierarchies, auto-configurations, and strict classpath dependency rules that frequently conflict during framework upgrades (e.g. migrating to Spring Boot 4.1.0).
*   **The RestClient Solution:** This microservice uses standard Spring Web `RestClient` to communicate directly with OpenAI-compatible gateway endpoints (OpenRouter). This completely decouples the app from vendor SDK releases.

### B. OpenAI-Compatible API Scheme (OpenRouter)
OpenRouter uses the standard `/v1/chat/completions` API structure shared by OpenAI:
- **Request Schema:** Send model identifier along with an array of historical role-content message pairs.
- **Response Schema:** A list of `choices`, where each choice contains a `message` holding the model's text response.

---

## 2. Technical Code Studies

### RestClient Integration (`AiProviderConfig.java`)
The custom RestClient is initialized as a bean with default authorization headers, media types, and base URLs resolved from properties:
```java
@Bean
public RestClient openRouterRestClient() {
    return RestClient.builder()
            .baseUrl(active.baseUrl())
            .defaultHeader("Authorization", "Bearer " + active.apiKey())
            .defaultHeader("Content-Type", "application/json")
            .build();
}
```

### JSON Schema Mapping DTOs (`AiController.java`)
Instead of using heavy domain entities, the JSON payload structures are mapped cleanly using Java `record` types nested inside the controller class:
*   `OpenRouterMessage`: Maps `{ "role": "user", "content": "..." }`.
*   `OpenRouterRequest`: Wraps model and message array.
*   `OpenRouterResponse` / `OpenRouterChoice`: Accesses the reply path.
*   **Parsing:** Automatic Jackson serialization/deserialization occurs transparently during `RestClient` request execution.
