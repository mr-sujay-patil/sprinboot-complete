# Task Service — CRUD Microservice with Best Practices

## Architecture
This is a Spring Boot resource server microservice. It validates JWTs from an external authorization server, and exposes a RESTful API for performing CRUD operations on a `Task` entity.

## Prerequisites
- Java 21+
- Maven

## Quick Start
Run the application using the Spring Boot Maven Plugin:
```bash
mvn spring-boot:run
```
The application runs on port `9092`.

## Best Practices Demonstrated
| Practice | Where in Code |
|----------|---------------|
| Layered Architecture | Controller → Service → Repository |
| DTO Pattern | TaskRequest, TaskResponse (entities never exposed) |
| Manual Mapper | TaskMapper — explicit, no magic |
| Global Exception Handling | GlobalExceptionHandler (@RestControllerAdvice) |
| Bean Validation | @Valid + constraints on TaskRequest |
| Pagination/Sorting | GET /api/tasks with Pageable |
| EntityManager Demo | TaskCustomRepository — when/why to use |
| Audit Fields | createdAt/updatedAt via @PrePersist/@PreUpdate |
| Enum as String | @Enumerated(EnumType.STRING) — safe for schema evolution |
| Resource Server JWT | JwtAuthenticationFilter — no DB, trusts token claims |

## Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/tasks` | Create task | Required |
| GET | `/api/tasks` | List tasks (paginated) | Required |
| GET | `/api/tasks/{id}` | Get task by ID | Required |
| PUT | `/api/tasks/{id}` | Update task | Required |
| DELETE | `/api/tasks/{id}` | Delete task | Required |
| GET | `/api/tasks/status/{status}` | Find by status | Required |
| GET | `/api/tasks/demo/entitymanager/{status}`| EntityManager demo | Required |

## H2 Console
You can access the in-memory database to inspect the tables and data.
- **URL**: [http://localhost:9092/h2-console](http://localhost:9092/h2-console)
- **JDBC URL**: `jdbc:h2:mem:taskdb`
- **Username**: `sa`
- **Password**: *(leave blank)*

## End-to-End Test Flow
1. **Get a JWT Token**: Obtain a valid JWT from the auth-server (running on port 9090).
2. **Execute CRUD operations**:
```bash
# Create a new task
curl -X POST http://localhost:9092/api/tasks \
     -H "Authorization: Bearer <YOUR_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"title": "Learn Spring", "status": "TODO"}'

# List tasks
curl -X GET http://localhost:9092/api/tasks \
     -H "Authorization: Bearer <YOUR_TOKEN>"
```

## Limitations
This is a learning project:
- Uses an H2 in-memory database (data is lost on restart).
- No caching layer implemented.
- No asynchronous messaging (e.g., Kafka or RabbitMQ).
