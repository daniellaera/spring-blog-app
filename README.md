# Spring Blog Application

[![Build Status](https://img.shields.io/github/workflow/status/daniellaera/spring-blog-app/CI)](https://github.com/daniellaera/spring-blog-app/actions)
[![Test Status](https://img.shields.io/github/workflow/status/daniellaera/spring-blog-app/Test)](https://github.com/daniellaera/spring-blog-app/actions)
[![Deploy Status](https://img.shields.io/github/workflow/status/daniellaera/spring-blog-app/Deploy)](https://github.com/daniellaera/spring-blog-app/actions)

This Spring Boot application serves as a blog platform, allowing users to create, read, update, and delete blog posts. Key features include user authentication, post categorization, and support for database migrations.

## Table of Contents

- [Project Setup](#project-setup)
- [Technologies Used](#technologies-used)
- [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [Building the Application](#building-the-application)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Deployment](#deployment)

## Project Setup

1. **Clone Repository**
   ```bash
   git clone https://github.com/your-repo/spring-blog-app.git
   cd spring-blog-app
   ```

2. **Set Up Environment Variables**
   Configure required environment variables in `.env` or in your deployment settings:
   - `DATABASE_URL`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`

## Technologies Used

- **Java** and **Spring Boot** for backend development
- **PostgreSQL** as the relational database
- **Flyway** for database migrations
- **JUnit** and **Mockito** for unit and integration testing
- **GitHub Actions** for CI/CD pipeline

## Running the Application

1. **Run the application locally** with:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Access the application** at `http://localhost:8080`.

## Configuration

The application settings are primarily managed through `application.yml` with environment-specific values for:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
```

## Building the Application

To build the application and create an executable JAR file:
```bash
./mvnw clean package
```

## Endpoints

### Public Endpoints
- `GET /posts` - Retrieves all blog posts
- `GET /posts/{id}` - Retrieves a specific post by ID

### Protected Endpoints
- `POST /posts` - Creates a new blog post (requires authentication)
- `PUT /posts/{id}` - Updates an existing post (requires authentication)
- `DELETE /posts/{id}` - Deletes a post (requires authentication)

## Testing

Run tests with:
```bash
./mvnw test
```
The application includes unit tests for service layers and integration tests for the repository layer, ensuring data consistency and business logic reliability.

## Deployment

The application is configured to deploy using **Fly.io** via GitHub Actions. Ensure that the following GitHub Secrets are set for the deployment pipeline:
- `FLY_API_TOKEN`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
