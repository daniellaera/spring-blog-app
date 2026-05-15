# Spring Blog App

> Passwordless blog platform built with Spring Boot 4 and Angular 21 using WebAuthn/Passkeys for authentication.

## Tech stack

**Backend**
- Java 21
- Spring Boot 4.0.6
- Spring Security 7
- PostgreSQL 17
- Flyway 11 (migrations + seed data)
- Yubico webauthn-server-core 2.9.0
- Testcontainers 2.0.5

**Frontend**
- Angular 21
- PrimeNG 21 (Aura theme)
- @simplewebauthn/browser 13
- PrimeFlex
- Inter font

## Features

- Passwordless authentication via WebAuthn/Passkeys (Face ID, Touch ID, device PIN)
- Passkey management — register, rename, delete credentials
- Passkey desync handling — graceful errors when credential missing from device or DB
- Blog posts — create, read, update, delete
- Comments on posts — create, read, update
- HTTP session-based auth (server-side session after passkey verification)
- Fully reactive Angular UI with signals

## Prerequisites

- Java 21+
- Node 20+
- PostgreSQL 17 running on `localhost:5432`
- A browser with WebAuthn support (Chrome, Safari, Firefox — all modern versions)

## Getting started

### 1. Clone the repo
```bash
git clone https://github.com/daniellaera/spring-blog-app.git
cd spring-blog-app
```

### 2. Start PostgreSQL

Start a local PostgreSQL instance and create the database:

```sql
CREATE DATABASE testdb;
CREATE USER testuser WITH PASSWORD 'testpass';
GRANT ALL PRIVILEGES ON DATABASE testdb TO testuser;
```

Or with Docker (one-off):

```bash
docker run -d \
  --name blog-postgres \
  -e POSTGRES_DB=testdb \
  -e POSTGRES_USER=testuser \
  -e POSTGRES_PASSWORD=testpass \
  -p 5432:5432 \
  postgres:17
```

### 3. Run the backend
```bash
cd backend
mvn spring-boot:run
```

Backend runs on http://localhost:8080.
Flyway runs all 9 migrations and seeds demo data automatically on startup.

### 4. Run the frontend
```bash
cd frontend
npm install
ng serve
```

Frontend runs on http://localhost:4200.

## Passkey flow

### Registration
1. Enter username on `/register`
2. Backend creates user account and returns a challenge (`POST /register/start`)
3. Browser prompts for Face ID / Touch ID / PIN
4. Passkey stored on device; public key verified and stored in DB (`POST /register/verify`)

### Authentication
1. Enter username on `/login`
2. Backend returns a challenge (`POST /login/start`)
3. Browser prompts for Face ID / Touch ID / PIN
4. Backend verifies signature and creates a server-side session (`POST /login/verify`)
5. Session cookie stored in browser; user redirected to app

## API endpoints

### Auth — Registration
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /register/start | No | Get registration challenge |
| POST | /register/verify | No | Complete registration |

### Auth — Login
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /login/start | No | Get authentication challenge |
| POST | /login/verify | No | Complete authentication, create session |
| GET | /session/me | Yes | Get current session user |

### Posts
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/v3/post | No | List all posts |
| GET | /api/v3/post/{id} | No | Get post by id |
| POST | /api/v3/post | Yes | Create post |
| PUT | /api/v3/post/{id} | Yes | Update post |
| DELETE | /api/v3/post/{id} | Yes | Delete post |

### Comments
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/v3/comment/{postId}/comments | No | List comments for a post |
| POST | /api/v3/comment/{postId} | Yes | Add comment to post |
| PATCH | /api/v3/comment/{commentId} | Yes | Update comment |

### Passkeys
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/user/passkeys | Yes | List user passkeys |
| PATCH | /api/user/passkeys/{id} | Yes | Rename passkey |
| DELETE | /api/user/passkeys/{id} | Yes | Remove passkey |
| DELETE | /api/user/passkeys/orphaned | Yes | Remove stale passkeys (unused 90+ days) |

## Project structure

```
├── backend/
│   ├── src/main/java/       Spring Boot app
│   ├── src/main/resources/
│   │   └── db/migration/    Flyway SQL migrations (V1–V9)
│   └── pom.xml
└── frontend/
    └── src/app/
        ├── core/            Interceptors, guards, services
        ├── features/        Auth, posts, account, user
        ├── layout/          Navbar
        └── shared/          Pipes, utils
```

## Running tests

```bash
cd backend && mvn test
cd frontend && ng test --watch=false
```

Tests use Testcontainers to spin up a real PostgreSQL instance — no mocking.

## Demo accounts

After running migrations, two demo accounts are seeded (V9):

| Username | Notes |
|----------|-------|
| daniel | Must register a passkey on first login |
| alice | Must register a passkey on first login |

> Passkeys are device-bound and cannot be seeded —
> use `/register` to create your passkey after the account exists.
