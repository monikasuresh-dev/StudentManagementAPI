# Architecture – StudentManagementAPI

The application has three layers plus a global error handler. There is **no database**: students are kept in a `ConcurrentHashMap` inside the running application, so all data is lost when it stops.

## Diagram (component view)

Ready-made images of the same diagram are in [`architecture.svg`](architecture.svg) and [`architecture.png`](architecture.png) (open it in a browser, or insert it into your report).

```mermaid
flowchart LR
    A["Client / Swagger UI"] -->|"HTTP + JSON"| B["StudentController<br/>(REST layer)"]
    B -->|"valid request"| C["StudentService<br/>(business logic)"]
    C -->|"read / write"| D[("In-memory store<br/>ConcurrentHashMap")]
    D --> C
    C --> B
    B -->|"200 / 201 / 204 + JSON"| A

    B -.->|"validation fails"| E["GlobalExceptionHandler"]
    C -.->|"StudentNotFoundException"| E
    E -.->|"400 / 404 + JSON error"| A
```

Solid arrows are the success path. Dotted arrows are the error path.

## Request–response flow (create a student)

```mermaid
sequenceDiagram
    participant U as Client / Swagger UI
    participant C as StudentController
    participant S as StudentService
    participant M as In-memory store
    participant E as GlobalExceptionHandler

    U->>C: POST /api/students (JSON)
    alt JSON is invalid (blank name, bad email, year outside 1-4)
        C->>E: MethodArgumentNotValidException
        E-->>U: 400 Bad Request (JSON error with details)
    else JSON is valid
        C->>S: create(student)
        S->>M: store with generated id
        M-->>S: saved student
        S-->>C: student with id
        C-->>U: 201 Created + Location header + JSON
    end
```

## Not-found path (get / update / delete by id)

```mermaid
sequenceDiagram
    participant U as Client
    participant C as StudentController
    participant S as StudentService
    participant E as GlobalExceptionHandler

    U->>C: GET /api/students/999
    C->>S: findById(999)
    S-->>C: throws StudentNotFoundException
    C->>E: exception handled
    E-->>U: 404 Not Found (JSON error)
```

## Components in simple terms

| Component | File | Role |
|---|---|---|
| Client / Swagger UI | browser | Sends HTTP requests and shows the responses. |
| `StudentController` | `controller/StudentController.java` | Receives HTTP requests, checks input with `@Valid`, calls the service, chooses the status code. |
| `StudentService` | `service/StudentService.java` | Contains the actual create/read/update/delete logic. Throws `StudentNotFoundException` if an id is missing. |
| `Student` | `model/Student.java` | The data (id, name, email, department, year) and its validation rules. |
| In-memory store | `ConcurrentHashMap` inside `StudentService` | Holds students while the app runs. An `AtomicLong` generates unique ids. |
| `GlobalExceptionHandler` | `exception/GlobalExceptionHandler.java` | Converts exceptions into clean JSON errors (no stack traces). |
| `ErrorResponse` | `exception/ErrorResponse.java` | The JSON shape of every error. |
| `OpenApiConfig` | `config/OpenApiConfig.java` | Title and description shown in Swagger UI. |

## Why the structure differs slightly from the task brief

Two small additions to the suggested layout: `exception/ErrorResponse.java` (one consistent error JSON shape) and `config/OpenApiConfig.java` (Swagger title/description). Everything else matches the brief.
