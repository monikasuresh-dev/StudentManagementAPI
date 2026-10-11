# API Testing and Demonstration Guide

> **Important:** every "Expected" block below is an *illustrative example* of what the API is designed to return. It is **not** a recorded result from a run. Replace it with your own screenshots and actual outputs after you run the project.
> All JSON values are **sample data**.

## 0. Start the application

```powershell
cd C:\path\to\StudentManagementAPI
.\mvnw.cmd spring-boot:run
```

Wait for a line similar to `Started StudentManagementApiApplication in ... seconds`.
Swagger UI: <http://localhost:8080/swagger-ui/index.html> · OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Students are stored in memory, so they disappear when you stop the app. IDs start at 1 on each run.

## 1. Using Swagger UI

Open the Swagger URL, click an endpoint, press **Try it out**, edit the input, press **Execute**, and read the **Server response** (code and body).

## 2. POST – create a student (expect 201)

Request body (sample data):

```json
{"name":"Monika S.","email":"monika@example.com","department":"Information Technology","year":2}
```

Expected: **201 Created**, a `Location: /api/students/1` header, and

```json
{"id":1,"name":"Monika S.","email":"monika@example.com","department":"Information Technology","year":2}
```

## 3. GET all (expect 200)

`GET /api/students` → **200 OK**, a JSON array containing the student(s) created so far (`[]` if none).

## 4. GET by id (expect 200)

`GET /api/students/1` → **200 OK** with the student JSON shown above.

## 5. PUT – update (expect 200)

`PUT /api/students/1` with sample data:

```json
{"name":"Monika S.","email":"monika@example.com","department":"Information Technology","year":3}
```

Expected: **200 OK**, same id, `"year":3`. Call GET by id again to confirm the change is stored.

## 6. DELETE (expect 204)

`DELETE /api/students/1` → **204 No Content** (empty body). `GET /api/students/1` afterwards → **404**.

## 7. Invalid input test (expect 400)

`POST /api/students` with sample data:

```json
{"name":"","email":"not-an-email","department":"IT","year":9}
```

Expected: **400 Bad Request**, similar to:

```json
{
  "timestamp": "2026-10-11T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "email: email must be a valid email address",
    "name: name must not be blank",
    "year: year must be between 1 and 4"
  ]
}
```

(The timestamp will differ.) Also try: a missing `year`, malformed JSON, and `GET /api/students/abc` or `/api/students/0` (all **400**).

## 8. Not-found test (expect 404)

`GET /api/students/999999` → **404 Not Found**:

```json
{
  "timestamp": "2026-10-11T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id 999999",
  "details": []
}
```

## 9. Status code summary

| Action | Success | Possible errors |
|---|---|---|
| POST /api/students | 201 | 400 |
| GET /api/students | 200 | – |
| GET /api/students/{id} | 200 | 400 (bad id), 404 |
| PUT /api/students/{id} | 200 | 400 (bad id/body), 404 |
| DELETE /api/students/{id} | 204 | 400 (bad id), 404 |

## 10. PowerShell alternative (optional)

Use `curl.exe` (not the `curl` alias) so JSON quotes survive:

```powershell
curl.exe -i -X POST http://localhost:8080/api/students -H "Content-Type: application/json" -d '{\"name\":\"Monika S.\",\"email\":\"monika@example.com\",\"department\":\"Information Technology\",\"year\":2}'
curl.exe -i http://localhost:8080/api/students
curl.exe -i http://localhost:8080/api/students/1
curl.exe -i -X DELETE http://localhost:8080/api/students/1
```

## 11. Automated tests

```powershell
.\mvnw.cmd test
```

Look for `Tests run: 16, Failures: 0, Errors: 0` and `BUILD SUCCESS`. Record your **actual** output here after running:

```
(paste your real test output here)
```
