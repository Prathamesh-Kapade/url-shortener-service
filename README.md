# URL Shortener Service

A production-grade URL shortener built with Spring Boot, featuring Redis caching,
async analytics via Apache Kafka, JWT authentication, and GeoIP tracking.

## Live Demo
> Deploy to Railway and add your URL here: `https://your-app.railway.app`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3, Java 21 |
| Database | PostgreSQL (JPA/Hibernate) |
| Cache | Redis (Cache-Aside pattern) |
| Messaging | Apache Kafka (async analytics) |
| Security | Spring Security + JWT |
| Docs | SpringDoc OpenAPI / Swagger |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, Docker Compose |

## Architecture

```
User → POST /api/auth/register → JWT Token
User → POST /api/shorten (Bearer token) → Short Code
User → GET /r/{code} → Redis → DB → 302 Redirect
                    ↓ async
              Kafka (click-events)
                    ↓
              Consumer → GeoIP → PostgreSQL
```

---

## Features

- **URL Shortening** — collision-safe 6-char alphanumeric codes
- **Redis Caching** — Cache-Aside with smart TTL matching URL expiry
- **JWT Auth** — register/login, Bearer token on all write endpoints
- **Async Analytics** — Kafka consumer captures IP, device, country per click
- **GeoIP Tracking** — country and city from IP via ip-api.com
- **GDPR Compliant** — last IP octet masked before storage
- **Swagger UI** — interactive API docs at `/swagger-ui/index.html`

## Getting Started

### Prerequisites
- Java 21
- Docker Desktop

### Run locally

```bash
# 1. Clone the repository
git clone https://github.com/your-username/urlshortenerservice.git
cd urlshortenerservice

# 2. Start infrastructure
docker-compose up -d

# 3. Run the application
./mvnw spring-boot:run

# App runs on http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui/index.html
```

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |

### URL Shortener
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/shorten` | Required | Shorten a URL |
| GET | `/r/{shortCode}` | Public | Redirect to original URL |
| GET | `/api/analytics/{shortCode}` | Required | Get click analytics |


## Environment Variables

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL |
| `SPRING_REDIS_HOST` | Redis host |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address |

---

## Running Tests

```bash
mvn test
# 18 unit tests — JUnit 5 + Mockito
```

---

## Design Decisions

**Why Redis Cache-Aside?**
URL shortener is read-heavy — one creation, thousands of redirects.
Cache-Aside only populates on read, maximizing cache efficiency.
Smart TTL aligns with URL expiry to prevent stale redirects.

**Why Kafka for analytics?**
GeoIP lookup takes ~100ms. Synchronous analytics would add that to every redirect.
Kafka decouples analytics from the critical redirect path — user gets 302 in 2ms.