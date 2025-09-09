# FourstackWarriors EventSphere

EventSphere is a Spring Boot web application that uses classic MVC with server-side rendering via Thymeleaf. It targets Java 21 and uses PostgreSQL for persistence. The codebase is organized into clear layers to keep the application maintainable and scalable.

## Table of Contents
- Overview
- Tech Stack
- Architecture and Package Structure
- Views and Templating
- Local Setup (Java 21 + PostgreSQL)
- Configuration
- How to Build and Run
- Testing
- Contributing
- License

## Overview
EventSphere provides web pages to create, browse, and manage event-related data. Controllers handle routes and populate models for the views; services encapsulate business logic; repositories manage data access; models represent domain entities; and DTOs act as form-backing and view models.

## Tech Stack
- Java 21
- Spring Boot (Spring MVC, Spring Data JPA)
- Thymeleaf (server-side templating)
- Jakarta Validation (form validation)
- PostgreSQL
- Maven
- Lombok

## Architecture and Package Structure

- controllers
  - Web layer exposing routes, processing form submissions, and returning view names.
  - Binds request data to DTOs, validates input, and fills the Model for templates.
  - Uses Post/Redirect/Get (PRG) after successful form submissions.

- dtos
  - Form-backing and view-specific models.
  - Carry Jakarta validation annotations (e.g., @NotBlank, @Email) for input validation.
  - Decouple the web layer from persistence entities.

- models
  - Domain and persistence entities (JPA).
  - Contain mappings and domain rules relevant to persistence.

- repositories
  - Spring Data JPA interfaces for CRUD and query methods.
  - Persistence-focused; no business logic.

- services
  - Business/application logic and orchestration.
  - Transactional boundaries and mapping between DTOs and entities.

## Views and Templating
- Thymeleaf templates live under src/main/resources/templates.
- Use fragments for layout (headers, footers, navigation).
- Bind forms with th:object and th:field and render validation feedback with th:errors.
- Static assets (CSS/JS/images) reside under src/main/resources/static.

## Local Setup (Java 21 + PostgreSQL)

Prerequisites:
- Java 21
- Maven 3.8+ (or Maven 3.9+ recommended)
- PostgreSQL 14+ (or compatible)

Database setup (example):
1. Create a database:
   - createdb eventsphere
2. Create a database user (optional, if not reusing an existing one):
   - createuser --pwprompt eventsphere_user
   - Grant privileges:
     - psql -d eventsphere -c "GRANT ALL PRIVILEGES ON DATABASE eventsphere TO eventsphere_user;"

## Configuration

Application properties (example for local development):