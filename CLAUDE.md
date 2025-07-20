# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

InvestIA is a Spring Boot web application for investors built with Java 17. It provides investment management features including user authentication, portfolio tracking, investment goals, and risk profiling.

## Project Structure

```
src/
├── main/java/com/InvestIA/
│   ├── InvestIaApplication.java      # Main Spring Boot application
│   ├── DTO/                          # Controllers
│   │   └── AuthController.java       # Authentication endpoints
│   ├── config/                       # Configuration classes
│   │   ├── ApplicationConfig.java    # Security and app configuration
│   │   ├── JwtAuthenticationFilter.java # JWT filter
│   │   ├── JwtService.java           # JWT utility service
│   │   └── SecurityConfig.java       # Spring Security configuration
│   ├── dto/auth/                     # Data Transfer Objects
│   │   ├── AuthResponse.java         # Authentication response
│   │   ├── LoginRequest.java         # Login request DTO
│   │   ├── RefreshTokenRequest.java  # Token refresh DTO
│   │   └── RegisterRequest.java      # Registration request DTO
│   ├── entity/                       # JPA entities
│   │   ├── Usuario.java              # User entity with Spring Security integration
│   │   ├── Perfil.java               # User risk profile
│   │   ├── Investimento.java         # Investment records
│   │   ├── Ativo.java                # Financial assets
│   │   └── Meta.java                 # Investment goals
│   ├── enums/                        # Enum definitions
│   │   ├── TipoPerfil.java           # Risk profile types (enhanced with descriptions)
│   │   ├── NivelExperiencia.java     # Experience levels (enhanced with levels)
│   │   ├── TipoAtivo.java            # Asset types (enhanced with categories)
│   │   ├── SetorAtivo.java           # Asset sectors (expanded list)
│   │   ├── StatusMeta.java           # Goal status
│   │   └── TipoInvestimento.java     # Investment transaction types
│   ├── repository/                   # Data access layer
│   │   ├── UsuarioRepository.java    # User data access
│   │   ├── PerfilRepository.java     # Profile data access
│   │   ├── AtivoRepository.java      # Asset data access
│   │   ├── InvestimentoRepository.java # Investment data access
│   │   └── MetaRepository.java       # Goal data access
│   └── service/                      # Business logic layer
│       └── AuthService.java          # Authentication business logic
├── main/resources/
│   └── application.properties        # Main configuration with JWT and DB settings
└── test/
    ├── java/com/InvestIA/
    │   └── InvestIaApplicationTests.java
    └── resources/
        └── application-test.properties  # Test configuration with H2
```

## Common Commands

### Build and Development
- **Build project**: `./mvnw clean compile`
- **Run application**: `./mvnw spring-boot:run`
- **Package application**: `./mvnw clean package`
- **Clean build artifacts**: `./mvnw clean`

### Testing
- **Run all tests**: `./mvnw test`
- **Run tests with verbose output**: `./mvnw test -X`
- **Run specific test**: `./mvnw test -Dtest=InvestIaApplicationTests`

### Maven Wrapper
Always use `./mvnw` (or `mvnw.cmd` on Windows) instead of `mvn` as the project includes Maven Wrapper.

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Java Version**: Java 17
- **Build Tool**: Maven with wrapper
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: Spring Security with JWT authentication
- **ORM**: Spring Data JPA with Hibernate
- **Documentation**: OpenAPI/Swagger (springdoc-openapi)
- **Testing**: JUnit 5 with Spring Boot Test
- **External APIs**: WebClient (Spring WebFlux)
- **Code Generation**: Lombok

## Architecture Notes

### Domain Model
- **Usuario**: User entity implementing Spring Security UserDetails
- **Perfil**: Risk assessment and investment preferences  
- **Investimento**: User's investment positions
- **Ativo**: Financial instruments (stocks, funds, etc.)
- **Meta**: Investment goals with AI-suggested strategies

### Key Features
- JWT-based authentication with Spring Security
- PostgreSQL database with UUID primary keys
- Investment portfolio tracking with real-time valuations
- Risk profiling system with tolerance scoring
- Investment goal setting with suggested monthly contributions
- Multi-asset support (stocks, REITs, fixed income, crypto, ETFs)

### Testing Configuration
- H2 in-memory database for tests (`application-test.properties`)
- `@ActiveProfiles("test")` for test isolation
- Hibernate DDL auto-generation for test schema

### Development Notes
- All entities use Lombok for boilerplate reduction
- UUIDs used for all primary keys
- Timestamp fields for audit trails
- Enum-based type safety for classifications
- Builder pattern available via Lombok (note @Builder.Default warnings)