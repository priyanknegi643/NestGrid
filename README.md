# NestGrid — Pure Java (Zero Dependencies)

Same features as the Spring Boot version, rewritten with nothing but the JDK.

## What changed (and what didn't)

| Concern | Spring Boot version | Pure Java version |
|---------|--------------------|--------------------|
| HTTP server | Spring MVC (Tomcat) | `com.sun.net.httpserver.HttpServer` (built into JDK) |
| DI / IoC | Spring Context | Plain constructor injection in `ServerConfig` |
| ORM / DB | JPA + H2 | In-memory `ConcurrentHashMap` + JSON flat-file (`data/*.json`) |
| Auth / JWT | Spring Security + jjwt | Hand-rolled HS256 (`javax.crypto.Mac`) |
| Password hashing | BCrypt | SHA-256 + 16-byte random salt (`java.security`) |
| JSON | Jackson | Hand-rolled `Json` parser/serialiser |
| Lombok | Lombok | Explicit getters/setters |
| Build | Maven | Single `javac` invocation (`run.sh`) |
| **REST API surface** | **identical** | **identical** |
| **Frontend files** | **identical** | **identical** |

## Project structure

```
nestgrid-pure/
├── run.sh                              ← compile + start backend
├── data/                               ← auto-created; stores users.json & accommodations.json
├── frontend/                           ← unchanged from Spring version
│   ├── auth.html
│   ├── index.html
│   ├── owner.html
│   ├── app.js
│   └── style.css
└── src/main/java/com/nestgrid/
    ├── NestGridApplication.java        ← entry point
    ├── config/
    │   ├── ServerConfig.java           ← wiring + HttpServer setup
    │   └── CorsWrapper.java            ← CORS + OPTIONS pre-flight
    ├── model/
    │   ├── User.java
    │   └── Accommodation.java
    ├── repository/
    │   ├── UserRepository.java         ← in-memory + JSON persistence
    │   └── AccommodationRepository.java
    ├── service/
    │   ├── AuthService.java
    │   ├── AccommodationService.java   ← CRUD + Haversine search + scoring
    │   └── DistanceService.java        ← Haversine formula
    ├── controller/
    │   ├── BaseHandler.java            ← shared auth/response helpers
    │   ├── AuthController.java         ← POST /api/auth/signup|login
    │   └── AccommodationController.java← GET/POST/PUT/DELETE /api/accommodations
    └── util/
        ├── Json.java                   ← hand-rolled JSON parser + serialiser
        ├── JwtUtil.java                ← HS256 JWT (HMAC-SHA256)
        └── PasswordUtil.java           ← SHA-256 + salt
```

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17 or 21 (JDK, not just JRE) |

No Maven, no Gradle, no external JARs.

## 1 — Run the backend

```bash
cd nestgrid-pure
chmod +x run.sh
./run.sh
```

Or manually:
```bash
mkdir -p target/classes
find src/main/java -name "*.java" > sources.txt
javac -d target/classes @sources.txt
java -cp target/classes com.nestgrid.NestGridApplication
```

Server starts on **http://localhost:8080**

Data is saved to `data/users.json` and `data/accommodations.json` automatically.

## 2 — Serve the frontend

```bash
cd nestgrid-pure/frontend
python3 -m http.server 5500
```

Then open **http://localhost:5500/auth.html**

## REST API (identical to Spring version)

### Auth (public)

| Method | URL | Body | Returns |
|--------|-----|------|---------|
| POST | `/api/auth/signup` | `{name, email, password, phone, role}` | `{token, name, email, role}` |
| POST | `/api/auth/login`  | `{email, password}` | `{token, name, email, role}` |

`role` must be `"USER"` or `"OWNER"`. All protected requests need:
```
Authorization: Bearer <token>
```

### Accommodations

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET    | `/api/accommodations`         | public  | All listings |
| POST   | `/api/accommodations/search`  | public  | Filtered + scored results |
| GET    | `/api/accommodations/my`      | OWNER   | Owner's own listings |
| POST   | `/api/accommodations`         | OWNER   | Create listing |
| PUT    | `/api/accommodations/{id}`    | OWNER   | Update own listing |
| DELETE | `/api/accommodations/{id}`    | OWNER   | Delete own listing |

### Search body example

```json
{
  "lat": 28.61, "lng": 77.20, "radius": 10, "type": "PG",
  "weightGym": 5, "weightHospital": 3, "weightGrocery": 2,
  "weightMetro": 0, "weightPark": 0, "weightSchool": 0
}
```

## Moving to production

- **Persistence** — replace the flat-file repositories with JDBC calls to any DB.
  The service layer is unchanged; only the repository layer needs swapping.
- **JWT secret** — edit `JwtUtil.SECRET` to a long random string.
- **CORS** — update `CorsWrapper.getAllowedOrigin()` to your actual frontend domain.
- **Threads** — `Executors.newFixedThreadPool(10)` in `ServerConfig`; adjust as needed.
