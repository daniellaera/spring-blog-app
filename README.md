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
- Account recovery via email OTP — regain access when passkey is lost
- Blog posts — create, read, update, delete
- Comments on posts — create, read, update
- HTTP session-based auth (server-side session after passkey verification)
- Fully reactive Angular UI with signals

## Local development setup

### Prerequisites
- Java 21+
- Node 20+
- Docker + Docker Compose
- A Gmail account with 2FA enabled (for email OTP recovery)
- A browser with WebAuthn support (Chrome, Safari, Firefox — all modern versions)

### 1. Clone the repo
```bash
git clone https://github.com/daniellaera/spring-blog-app.git
cd spring-blog-app
```

### 2. Configure the backend
```bash
cp backend/src/main/resources/application-dev.yml.example \
   backend/src/main/resources/application-dev.yml
```

```bash
cp backend/src/main/resources/application-dev.yml.example \
   backend/src/main/resources/application-dev.yml
```

Then open `application-dev.yml` and replace every placeholder:
- `YOUR_DB_PASSWORD` → your PostgreSQL password
- `YOUR_GMAIL@gmail.com` → your Gmail address
- `YOUR_16_CHAR_APP_PASSWORD` → your Gmail App Password
- Any other `YOUR_*` values

> `application-dev.yml` is gitignored — it will never be committed.
> `application-dev.yml.example` is the safe template to track in git.

### 3. Generate a Gmail App Password

Required for the account recovery email OTP feature:
1. Go to myaccount.google.com
2. Security → 2-Step Verification (must be enabled first)
3. App passwords → Generate
4. Or go directly to: https://myaccount.google.com/apppasswords
5. Copy the 16-character password into `application-dev.yml`

### 4. Start the database
```bash
docker-compose -f docker-compose.db.yml up -d
```

### 5. Start the backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway runs automatically on startup:
- Creates all tables
- Seeds demo users (daniel, alice)

> Note: demo users have no passkeys — register one at `/register`

### 6. Start the frontend
```bash
cd frontend
npm install
ng serve
```

App runs at http://localhost:4200

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

## Account recovery flow

When a user loses access to their passkey (deleted from device, new phone, etc):

1. Go to `/login`
2. Enter username and attempt sign in
3. Cancel the passkey prompt
4. Recovery dialog appears automatically
5. Enter email address
6. Receive 6-digit OTP by email (in dev mode: check backend logs)
7. Enter OTP code
8. Register a new passkey on the current device
9. Sign in normally

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

### Auth — Account Recovery
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /recovery/start | No | Send OTP to user's registered email |
| POST | /recovery/verify | No | Verify OTP and enable passkey re-registration |

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
│   │   └── db/migration/    Flyway SQL migrations (V1–V10)
│   └── pom.xml
└── frontend/
    └── src/app/
        ├── core/            Interceptors, guards, services
        ├── features/        Auth, posts, account, user
        ├── layout/          Navbar
        └── shared/          Pipes, utils
```

## Environment files

| File | Committed | Purpose |
|------|-----------|---------|
| application.yml | ✅ Yes | Base config, no secrets |
| application-dev.yml | ❌ No | Local secrets (DB, Gmail) |
| application-dev.yml.example | ✅ Yes | Template for new developers |

## Security notes

- Passkeys are device-bound — no passwords stored anywhere
- This app uses server-side sessions — no JWT tokens
- OTP codes expire after 10 minutes and are single-use
- Gmail credentials are never committed to git
- `application-dev.yml` is in `.gitignore`

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
